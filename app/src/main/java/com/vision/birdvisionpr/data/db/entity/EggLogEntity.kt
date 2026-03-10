package com.vision.birdvisionpr.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "egg_logs")
data class EggLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val count: Int,
    val totalBirds: Int,
    val notes: String = ""
)
