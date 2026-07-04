package com.example.data.repository

import com.example.data.database.SpeedtestDao
import com.example.data.database.SpeedtestEntity
import com.example.data.database.TransitionDao
import com.example.data.database.TransitionEntity
import com.example.data.model.PingResult
import com.example.data.model.SpeedtestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

class NetworkRepository(
    private val transitionDao: TransitionDao,
    private val speedtestDao: SpeedtestDao
) {
    val allTransitions: Flow<List<TransitionEntity>> = transitionDao.getAllTransitions()
    val allSpeedtests: Flow<List<SpeedtestEntity>> = speedtestDao.getAllSpeedtests()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun insertTransition(transition: TransitionEntity) = withContext(Dispatchers.IO) {
        transitionDao.insertTransition(transition)
        transitionDao.pruneOldTransitions()
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        transitionDao.clearAll()
        speedtestDao.clearAll()
    }

    suspend fun pingOnce(target: String): PingResult = withContext(Dispatchers.IO) {
        // 1. Try system ping
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -W 1 $target")
            val output = StringBuilder()
            process.inputStream.bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
            }
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                val regex = "time=([0-9.]+)".toRegex()
                val match = regex.find(output.toString())
                val latency = match?.groupValues?.get(1)?.toDoubleOrNull()
                return@withContext PingResult(isOnline = true, latencyMs = latency ?: 10.0)
            }
        } catch (e: Exception) {
            // Fallback
        } finally {
            process?.destroy()
        }

        // 2. Socket connection fallback (DNS port 53)
        try {
            val startTime = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress(target, 53), 1000)
            val latency = (System.currentTimeMillis() - startTime).toDouble()
            socket.close()
            return@withContext PingResult(isOnline = true, latencyMs = latency)
        } catch (e: Exception) {
            // Try HTTP port 80 fallback
            try {
                val startTime = System.currentTimeMillis()
                val socket = Socket()
                socket.connect(InetSocketAddress(target, 80), 1000)
                val latency = (System.currentTimeMillis() - startTime).toDouble()
                socket.close()
                return@withContext PingResult(isOnline = true, latencyMs = latency)
            } catch (e2: Exception) {
                return@withContext PingResult(isOnline = false, latencyMs = null)
            }
        }
    }

    suspend fun runSpeedtest(
        onStateChange: (SpeedtestState) -> Unit
    ) = withContext(Dispatchers.IO) {
        onStateChange(SpeedtestState.Pinging)

        var pingLatency = 0.0
        try {
            val startTime = System.currentTimeMillis()
            val request = Request.Builder().url("https://speed.cloudflare.com/").build()
            okHttpClient.newCall(request).execute().use { response ->
                pingLatency = if (response.isSuccessful) {
                    (System.currentTimeMillis() - startTime).toDouble()
                } else {
                    50.0
                }
            }
        } catch (e: Exception) {
            pingLatency = 100.0
        }

        // Download speed test (5MB)
        onStateChange(SpeedtestState.Downloading(currentSpeedMbps = 0.0, progress = 0f))
        var downloadMbps = 0.0
        try {
            val request = Request.Builder()
                .url("https://speed.cloudflare.com/__down?bytes=5000000")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed download speed test")
                val body = response.body ?: throw IOException("Download test body is null")
                val totalBytes = body.contentLength().coerceAtLeast(5000000L)
                val stream = body.byteStream()
                val buffer = ByteArray(16384)
                var bytesRead = 0L
                val testStartTime = System.currentTimeMillis()
                var lastUpdate = System.currentTimeMillis()

                while (true) {
                    val read = stream.read(buffer)
                    if (read == -1) break
                    bytesRead += read

                    val now = System.currentTimeMillis()
                    if (now - lastUpdate > 100 || bytesRead == totalBytes) {
                        val elapsedSec = (now - testStartTime) / 1000.0
                        val speed = if (elapsedSec > 0) {
                            (bytesRead * 8.0) / (elapsedSec * 1_000_000.0)
                        } else 0.0
                        val progress = bytesRead.toFloat() / totalBytes
                        onStateChange(SpeedtestState.Downloading(speed, progress))
                        lastUpdate = now
                    }
                }
                val finalElapsedSec = (System.currentTimeMillis() - testStartTime) / 1000.0
                downloadMbps = (bytesRead * 8.0) / (finalElapsedSec * 1_000_000.0)
            }
        } catch (e: Exception) {
            onStateChange(SpeedtestState.Failed("Download failed: ${e.localizedMessage}"))
            return@withContext
        }

        // Upload speed test (2MB)
        onStateChange(SpeedtestState.Uploading(currentSpeedMbps = 0.0, progress = 0f))
        var uploadMbps = 0.0
        try {
            val uploadBytesCount = 2000000L
            val requestBody = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaType()
                override fun contentLength() = uploadBytesCount
                override fun writeTo(sink: okio.BufferedSink) {
                    val buffer = ByteArray(16384)
                    var written = 0L
                    val testStartTime = System.currentTimeMillis()
                    var lastUpdate = System.currentTimeMillis()

                    while (written < uploadBytesCount) {
                        val toWrite = minOf(buffer.size.toLong(), uploadBytesCount - written).toInt()
                        sink.write(buffer, 0, toWrite)
                        written += toWrite

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 100 || written == uploadBytesCount) {
                            val elapsedSec = (now - testStartTime) / 1000.0
                            val speed = if (elapsedSec > 0) {
                                (written * 8.0) / (elapsedSec * 1_000_000.0)
                            } else 0.0
                            val progress = written.toFloat() / uploadBytesCount
                            onStateChange(SpeedtestState.Uploading(speed, progress))
                            lastUpdate = now
                        }
                    }
                }
            }

            val request = Request.Builder()
                .url("https://speed.cloudflare.com/__up")
                .post(requestBody)
                .build()

            val testStartTime = System.currentTimeMillis()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed upload speed test")
                val finalElapsedSec = (System.currentTimeMillis() - testStartTime) / 1000.0
                uploadMbps = (uploadBytesCount * 8.0) / (finalElapsedSec * 1_000_000.0)
            }
        } catch (e: Exception) {
            onStateChange(SpeedtestState.Failed("Upload failed: ${e.localizedMessage}"))
            return@withContext
        }

        val finalDownload = downloadMbps
        val finalUpload = uploadMbps
        val finalPing = pingLatency

        val entity = SpeedtestEntity(
            downloadSpeedMbps = finalDownload,
            uploadSpeedMbps = finalUpload,
            pingLatencyMs = finalPing
        )
        speedtestDao.insertSpeedtest(entity)
        speedtestDao.pruneOldSpeedtests()

        onStateChange(SpeedtestState.Completed(finalDownload, finalUpload, finalPing))
    }
}
