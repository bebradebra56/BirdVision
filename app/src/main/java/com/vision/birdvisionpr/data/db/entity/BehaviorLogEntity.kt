package com.vision.birdvisionpr.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "behavior_logs")
data class BehaviorLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val behaviorType: String,
    val severity: Int,
    val birdCount: Int,
    val notes: String = ""
)
