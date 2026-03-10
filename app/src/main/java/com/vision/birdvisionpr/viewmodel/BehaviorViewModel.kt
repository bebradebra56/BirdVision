package com.vision.birdvisionpr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vision.birdvisionpr.data.db.entity.BehaviorLogEntity
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BehaviorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BirdVisionApplication).repository

    val behaviorLogs = repository.allBehaviorLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(
        behaviorType: String,
        severity: Int,
        birdCount: Int,
        notes: String
    ) = viewModelScope.launch {
        repository.insertBehaviorLog(
            BehaviorLogEntity(
                behaviorType = behaviorType,
                severity = severity,
                birdCount = birdCount,
                notes = notes
            )
        )
    }

    fun deleteEntry(entry: BehaviorLogEntity) = viewModelScope.launch {
        repository.deleteBehaviorLog(entry)
    }
}
