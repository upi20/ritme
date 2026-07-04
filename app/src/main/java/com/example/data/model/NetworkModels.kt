package com.example.data.model

data class PingResult(
    val isOnline: Boolean,
    val latencyMs: Double?,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface SpeedtestState {
    object Idle : SpeedtestState
    object Pinging : SpeedtestState
    data class Downloading(val currentSpeedMbps: Double, val progress: Float) : SpeedtestState
    data class Uploading(val currentSpeedMbps: Double, val progress: Float) : SpeedtestState
    data class Completed(val downloadMbps: Double, val uploadMbps: Double, val latencyMs: Double) : SpeedtestState
    data class Failed(val error: String) : SpeedtestState
}
