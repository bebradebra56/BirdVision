package com.vision.birdvisionpr.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklists")
data class ChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val water: Boolean = false,
    val feed: Boolean = false,
    val ventilation: Boolean = false,
    val cleanliness: Boolean = false,
    val nestBoxes: Boolean = false,
    val lighting: Boolean = false,
    val notes: String = ""
)
