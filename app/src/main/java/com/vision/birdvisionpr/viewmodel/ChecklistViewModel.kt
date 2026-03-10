package com.vision.birdvisionpr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vision.birdvisionpr.data.db.entity.ChecklistEntity
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BirdVisionApplication).repository

    val allChecklists = repository.allChecklists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestChecklist = repository.latestChecklist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getOrCreateTodayChecklist(onResult: (ChecklistEntity) -> Unit) = viewModelScope.launch {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val dayEnd = cal.timeInMillis

        val existing = repository.getChecklistForDay(dayStart, dayEnd)
        if (existing != null) {
            onResult(existing)
        } else {
            val id = repository.insertChecklist(ChecklistEntity(date = System.currentTimeMillis()))
            onResult(ChecklistEntity(id = id, date = System.currentTimeMillis()))
        }
    }

    fun updateChecklist(checklist: ChecklistEntity) = viewModelScope.launch {
        repository.updateChecklist(checklist)
    }

    fun deleteChecklist(checklist: ChecklistEntity) = viewModelScope.launch {
        repository.deleteChecklist(checklist)
    }
}
