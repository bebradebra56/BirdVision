package com.vision.birdvisionpr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vision.birdvisionpr.data.db.entity.TemperatureLogEntity
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemperatureViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BirdVisionApplication).repository

    val temperatureLogs = repository.allTemperatureLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestTemperature = repository.latestTemperature
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addEntry(temperature: Float, humidity: Float) = viewModelScope.launch {
        repository.insertTemperatureLog(
            TemperatureLogEntity(temperature = temperature, humidity = humidity)
        )
    }

    fun deleteEntry(entry: TemperatureLogEntity) = viewModelScope.launch {
        repository.deleteTemperatureLog(entry)
    }
}
