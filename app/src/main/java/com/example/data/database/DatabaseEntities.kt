package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transitions")
data class TransitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: Boolean, // true = online, false = offline
    val previousStateDurationMs: Long,
    val formattedLog: String
)

@Entity(tableName = "speedtests")
data class SpeedtestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadSpeedMbps: Double,
    val uploadSpeedMbps: Double,
    val pingLatencyMs: Double
)
