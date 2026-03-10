package com.vision.birdvisionpr.data.db.dao

import androidx.room.*
import com.vision.birdvisionpr.data.db.entity.TemperatureLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemperatureLogDao {
    @Query("SELECT * FROM temperature_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TemperatureLogEntity>>

    @Query("SELECT * FROM temperature_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<TemperatureLogEntity>>

    @Query("SELECT * FROM temperature_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<TemperatureLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TemperatureLogEntity)

    @Delete
    suspend fun delete(entry: TemperatureLogEntity)
}
