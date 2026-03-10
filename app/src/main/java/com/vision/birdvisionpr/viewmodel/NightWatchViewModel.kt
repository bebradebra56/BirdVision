package com.vision.birdvisionpr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vision.birdvisionpr.data.db.entity.NightWatchEntity
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NightWatchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BirdVisionApplication).repository

    val nightWatchLogs = repository.allNightWatchLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(eventType: String, severity: Int, notes: String) = viewModelScope.launch {
        repository.insertNightWatchLog(
            NightWatchEntity(eventType = eventType, severity = severity, notes = notes)
        )
    }

    fun deleteEntry(entry: NightWatchEntity) = viewModelScope.launch {
        repository.deleteNightWatchLog(entry)
    }
}
