package com.vision.birdvisionpr.data.db.dao

import androidx.room.*
import com.vision.birdvisionpr.data.db.entity.BehaviorLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BehaviorLogDao {
    @Query("SELECT * FROM behavior_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BehaviorLogEntity>>

    @Query("SELECT * FROM behavior_logs WHERE timestamp >= :from AND timestamp <= :to ORDER BY timestamp DESC")
    fun getInRange(from: Long, to: Long): Flow<List<BehaviorLogEntity>>

    @Query("SELECT * FROM behavior_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<BehaviorLogEntity>>

    @Query("SELECT AVG(severity) FROM behavior_logs WHERE timestamp >= :from")
    suspend fun getAvgSeveritySince(from: Long): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BehaviorLogEntity)

    @Delete
    suspend fun delete(entry: BehaviorLogEntity)
}
