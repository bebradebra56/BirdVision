package com.vision.birdvisionpr.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temperature_logs")
data class TemperatureLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val temperature: Float,
    val humidity: Float
)
