package com.vision.birdvisionpr.data.db.dao

import androidx.room.*
import com.vision.birdvisionpr.data.db.entity.NightWatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NightWatchDao {
    @Query("SELECT * FROM night_watch_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NightWatchEntity>>

    @Query("SELECT * FROM night_watch_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<NightWatchEntity>>

    @Query("SELECT COUNT(*) FROM night_watch_logs WHERE timestamp >= :from")
    suspend fun getCountSince(from: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: NightWatchEntity)

    @Delete
    suspend fun delete(entry: NightWatchEntity)
}
