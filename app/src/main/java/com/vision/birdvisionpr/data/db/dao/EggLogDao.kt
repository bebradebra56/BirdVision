package com.vision.birdvisionpr.data.db.dao

import androidx.room.*
import com.vision.birdvisionpr.data.db.entity.EggLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EggLogDao {
    @Query("SELECT * FROM egg_logs ORDER BY date DESC")
    fun getAll(): Flow<List<EggLogEntity>>

    @Query("SELECT * FROM egg_logs WHERE date >= :from AND date <= :to ORDER BY date DESC")
    fun getInRange(from: Long, to: Long): Flow<List<EggLogEntity>>

    @Query("SELECT * FROM egg_logs ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<EggLogEntity>>

    @Query("SELECT AVG(CAST(count AS FLOAT) / CAST(totalBirds AS FLOAT)) FROM egg_logs WHERE date >= :from")
    suspend fun getAvgRateSince(from: Long): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EggLogEntity)

    @Delete
    suspend fun delete(entry: EggLogEntity)
}
