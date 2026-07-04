package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.TransitionEntity
import com.example.data.model.PingResult
import com.example.data.model.SpeedtestState
import com.example.data.repository.NetworkRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class RitmeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NetworkRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NetworkRepository(database.transitionDao(), database.speedtestDao())
    }

    val transitionLogs: StateFlow<List<TransitionEntity>> = repository.allTransitions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val speedtestHistory = repository.allSpeedtests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Configurable state
    private val _target = MutableStateFlow("8.8.8.8")
    val target: StateFlow<String> = _target.asStateFlow()

    private val _pingIntervalMs = MutableStateFlow(1000L)
    val pingIntervalMs: StateFlow<Long> = _pingIntervalMs.asStateFlow()

    // Ping loop variables
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _suksesCount = MutableStateFlow(0)
    val suksesCount: StateFlow<Int> = _suksesCount.asStateFlow()

    private val _gagalCount = MutableStateFlow(0)
    val gagalCount: StateFlow<Int> = _gagalCount.asStateFlow()

    private val _latencies = MutableStateFlow<List<Double>>(emptyList())
    val latencies: StateFlow<List<Double>> = _latencies.asStateFlow()

    private val _sparklineData = MutableStateFlow<List<Boolean>>(emptyList())
    val sparklineData: StateFlow<List<Boolean>> = _sparklineData.asStateFlow()

    private val _statusTerakhir = MutableStateFlow<Boolean?>(null)
    val statusTerakhir: StateFlow<Boolean?> = _statusTerakhir.asStateFlow()

    private val _waktuPerubahan = MutableStateFlow(System.currentTimeMillis())
    val waktuPerubahan: StateFlow<Long> = _waktuPerubahan.asStateFlow()

    private val _sessionStart = MutableStateFlow(System.currentTimeMillis())
    val sessionStart: StateFlow<Long> = _sessionStart.asStateFlow()

    // Speedtest state
    private val _speedtestState = MutableStateFlow<SpeedtestState>(SpeedtestState.Idle)
    val speedtestState: StateFlow<SpeedtestState> = _speedtestState.asStateFlow()

    private var pingJob: Job? = null
    private var autoSpeedtestJob: Job? = null

    init {
        startPingLoop()
        startAutoSpeedtestLoop()
    }

    fun startPingLoop() {
        pingJob?.cancel()
        pingJob = viewModelScope.launch {
            while (true) {
                if (!_isPaused.value) {
                    val currentTarget = _target.value
                    val result = repository.pingOnce(currentTarget)
                    handlePingResult(result)
                }
                delay(_pingIntervalMs.value)
            }
        }
    }

    fun startAutoSpeedtestLoop() {
        autoSpeedtestJob?.cancel()
        autoSpeedtestJob = viewModelScope.launch {
            while (true) {
                delay(3 * 60 * 1000L) // 3 minutes interval
                if (!_isPaused.value) {
                    startSpeedtest()
                }
            }
        }
    }

    private suspend fun handlePingResult(result: PingResult) {
        val now = System.currentTimeMillis()

        // Update counts
        if (result.isOnline) {
            _suksesCount.value += 1
        } else {
            _gagalCount.value += 1
        }

        // Latency history
        val updatedLatencies = _latencies.value.toMutableList()
        if (result.latencyMs != null) {
            updatedLatencies.add(result.latencyMs)
            if (updatedLatencies.size > 100) {
                updatedLatencies.removeAt(0)
            }
        }
        _latencies.value = updatedLatencies

        // Sparkline
        val updatedSparkline = _sparklineData.value.toMutableList()
        updatedSparkline.add(result.isOnline)
        if (updatedSparkline.size > 100) {
            updatedSparkline.removeAt(0)
        }
        _sparklineData.value = updatedSparkline

        // Check for Transitions
        val previousStatus = _statusTerakhir.value
        val currentStatus = result.isOnline

        if (previousStatus != null && previousStatus != currentStatus) {
            val lastChangeTime = _waktuPerubahan.value
            val durationMs = now - lastChangeTime
            val durationStr = formatDuration(durationMs / 1000)

            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeStr = timeFormat.format(Date(now))

            val statusStr = if (currentStatus) "NYAMBUNG" else "TERPUTUS"
            val logMessage = "$timeStr  $statusStr (sebelumnya $durationStr)"

            // Save log to Room DB
            repository.insertTransition(
                TransitionEntity(
                    status = currentStatus,
                    previousStateDurationMs = durationMs,
                    formattedLog = logMessage
                )
            )

            _waktuPerubahan.value = now
        } else if (previousStatus == null) {
            _waktuPerubahan.value = now
        }

        _statusTerakhir.value = currentStatus
    }

    fun startSpeedtest() {
        if (_speedtestState.value is SpeedtestState.Downloading ||
            _speedtestState.value is SpeedtestState.Uploading ||
            _speedtestState.value is SpeedtestState.Pinging
        ) {
            return // already running
        }
        viewModelScope.launch {
            repository.runSpeedtest { state ->
                _speedtestState.value = state
            }
        }
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun resetStats() {
        viewModelScope.launch {
            _suksesCount.value = 0
            _gagalCount.value = 0
            _latencies.value = emptyList()
            _sparklineData.value = emptyList()
            _statusTerakhir.value = null
            _waktuPerubahan.value = System.currentTimeMillis()
            _sessionStart.value = System.currentTimeMillis()
            _speedtestState.value = SpeedtestState.Idle
            repository.clearHistory()
        }
    }

    fun updateTarget(newTarget: String) {
        if (newTarget.isNotBlank() && _target.value != newTarget) {
            _target.value = newTarget.trim()
            // Reset state for new target
            _suksesCount.value = 0
            _gagalCount.value = 0
            _latencies.value = emptyList()
            _sparklineData.value = emptyList()
            _statusTerakhir.value = null
            _waktuPerubahan.value = System.currentTimeMillis()
            startPingLoop()
        }
    }

    fun updateInterval(newIntervalSec: Long) {
        val intervalMs = (newIntervalSec * 1000).coerceIn(500, 10000)
        _pingIntervalMs.value = intervalMs
    }

    // Helper functions
    fun formatDuration(seconds: Long): String {
        return if (seconds < 60) {
            "${seconds}dtk"
        } else if (seconds < 3600) {
            val m = seconds / 60
            val s = seconds % 60
            "${m}m${s}dtk"
        } else {
            val h = seconds / 3600
            val r = seconds % 3600
            val m = r / 60
            "${h}j${m}m"
        }
    }

    fun getJitter(): Double {
        val list = _latencies.value
        if (list.size < 2) return 0.0
        var sumDiff = 0.0
        for (i in 1 until list.size) {
            sumDiff += abs(list[i] - list[i - 1])
        }
        return sumDiff / (list.size - 1)
    }

    fun getLatencyBucketCounts(): List<Int> {
        val list = _latencies.value
        val buckets = IntArray(7)
        for (lat in list) {
            when {
                lat < 10.0 -> buckets[0]++
                lat < 25.0 -> buckets[1]++
                lat < 50.0 -> buckets[2]++
                lat < 100.0 -> buckets[3]++
                lat < 200.0 -> buckets[4]++
                lat < 500.0 -> buckets[5]++
                else -> buckets[6]++
            }
        }
        return buckets.toList()
    }

    override fun onCleared() {
        super.onCleared()
        pingJob?.cancel()
        autoSpeedtestJob?.cancel()
    }
}
