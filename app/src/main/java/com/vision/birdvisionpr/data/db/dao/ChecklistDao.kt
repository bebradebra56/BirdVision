package com.vision.birdvisionpr.data.db.dao

import androidx.room.*
import com.vision.birdvisionpr.data.db.entity.ChecklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklists ORDER BY date DESC")
    fun getAll(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklists ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<ChecklistEntity?>

    @Query("SELECT * FROM checklists WHERE date >= :dayStart AND date <= :dayEnd LIMIT 1")
    suspend fun getForDay(dayStart: Long, dayEnd: Long): ChecklistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ChecklistEntity): Long

    @Update
    suspend fun update(entry: ChecklistEntity)

    @Delete
    suspend fun delete(entry: ChecklistEntity)
}
