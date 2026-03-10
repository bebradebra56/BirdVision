package com.vision.birdvisionpr.data.repository

import com.vision.birdvisionpr.data.db.AppDatabase
import com.vision.birdvisionpr.data.db.entity.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    // Behavior
    val allBehaviorLogs: Flow<List<BehaviorLogEntity>> = db.behaviorLogDao().getAll()
    fun recentBehaviorLogs(limit: Int = 30) = db.behaviorLogDao().getRecent(limit)
    fun behaviorLogsInRange(from: Long, to: Long) = db.behaviorLogDao().getInRange(from, to)
    suspend fun insertBehaviorLog(entry: BehaviorLogEntity) = db.behaviorLogDao().insert(entry)
    suspend fun deleteBehaviorLog(entry: BehaviorLogEntity) = db.behaviorLogDao().delete(entry)
    suspend fun avgBehaviorSeveritySince(from: Long) = db.behaviorLogDao().getAvgSeveritySince(from)

    // Eggs
    val allEggLogs: Flow<List<EggLogEntity>> = db.eggLogDao().getAll()
    fun recentEggLogs(limit: Int = 30) = db.eggLogDao().getRecent(limit)
    fun eggLogsInRange(from: Long, to: Long) = db.eggLogDao().getInRange(from, to)
    suspend fun insertEggLog(entry: EggLogEntity) = db.eggLogDao().insert(entry)
    suspend fun deleteEggLog(entry: EggLogEntity) = db.eggLogDao().delete(entry)
    suspend fun avgEggRateSince(from: Long) = db.eggLogDao().getAvgRateSince(from)

    // Temperature
    val allTemperatureLogs: Flow<List<TemperatureLogEntity>> = db.temperatureLogDao().getAll()
    val latestTemperature: Flow<TemperatureLogEntity?> = db.temperatureLogDao().getLatest()
    fun recentTemperatureLogs(limit: Int = 20) = db.temperatureLogDao().getRecent(limit)
    suspend fun insertTemperatureLog(entry: TemperatureLogEntity) = db.temperatureLogDao().insert(entry)
    suspend fun deleteTemperatureLog(entry: TemperatureLogEntity) = db.temperatureLogDao().delete(entry)

    // Night Watch
    val allNightWatchLogs: Flow<List<NightWatchEntity>> = db.nightWatchDao().getAll()
    fun recentNightWatchLogs(limit: Int = 20) = db.nightWatchDao().getRecent(limit)
    suspend fun insertNightWatchLog(entry: NightWatchEntity) = db.nightWatchDao().insert(entry)
    suspend fun deleteNightWatchLog(entry: NightWatchEntity) = db.nightWatchDao().delete(entry)
    suspend fun nightWatchCountSince(from: Long) = db.nightWatchDao().getCountSince(from)

    // Checklist
    val allChecklists: Flow<List<ChecklistEntity>> = db.checklistDao().getAll()
    val latestChecklist: Flow<ChecklistEntity?> = db.checklistDao().getLatest()
    suspend fun getChecklistForDay(dayStart: Long, dayEnd: Long) = db.checklistDao().getForDay(dayStart, dayEnd)
    suspend fun insertChecklist(entry: ChecklistEntity): Long = db.checklistDao().insert(entry)
    suspend fun updateChecklist(entry: ChecklistEntity) = db.checklistDao().update(entry)
    suspend fun deleteChecklist(entry: ChecklistEntity) = db.checklistDao().delete(entry)
}
