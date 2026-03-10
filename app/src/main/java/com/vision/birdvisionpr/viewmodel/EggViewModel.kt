package com.vision.birdvisionpr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vision.birdvisionpr.data.db.entity.EggLogEntity
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EggViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BirdVisionApplication).repository

    val eggLogs = repository.allEggLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(count: Int, totalBirds: Int, notes: String) = viewModelScope.launch {
        repository.insertEggLog(
            EggLogEntity(count = count, totalBirds = totalBirds, notes = notes)
        )
    }

    fun deleteEntry(entry: EggLogEntity) = viewModelScope.launch {
        repository.deleteEggLog(entry)
    }
}
