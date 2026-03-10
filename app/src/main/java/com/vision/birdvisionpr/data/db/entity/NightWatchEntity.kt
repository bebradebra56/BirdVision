package com.vision.birdvisionpr.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "night_watch_logs")
data class NightWatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val severity: Int,
    val notes: String = ""
)
