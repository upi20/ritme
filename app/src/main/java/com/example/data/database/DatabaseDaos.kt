package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransitionDao {
    @Query("SELECT * FROM transitions ORDER BY timestamp DESC LIMIT 50")
    fun getAllTransitions(): Flow<List<TransitionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransition(transition: TransitionEntity)

    @Query("DELETE FROM transitions WHERE id NOT IN (SELECT id FROM transitions ORDER BY timestamp DESC LIMIT 100)")
    suspend fun pruneOldTransitions()

    @Query("DELETE FROM transitions")
    suspend fun clearAll()
}

@Dao
interface SpeedtestDao {
    @Query("SELECT * FROM speedtests ORDER BY timestamp DESC LIMIT 30")
    fun getAllSpeedtests(): Flow<List<SpeedtestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedtest(speedtest: SpeedtestEntity)

    @Query("DELETE FROM speedtests WHERE id NOT IN (SELECT id FROM speedtests ORDER BY timestamp DESC LIMIT 100)")
    suspend fun pruneOldSpeedtests()

    @Query("DELETE FROM speedtests")
    suspend fun clearAll()
}
