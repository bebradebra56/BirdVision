package com.vision.birdvisionpr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vision.birdvisionpr.domain.analyzer.ProblemAnalyzer
import com.vision.birdvisionpr.domain.analyzer.StressCalculator
import com.vision.birdvisionpr.domain.model.RiskLevel
import com.vision.birdvisionpr.domain.model.StressBreakdown
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeState(
    val stressIndex: Int = 0,
    val stressLevel: RiskLevel = RiskLevel.GOOD,
    val activityLevel: RiskLevel = RiskLevel.GOOD,
    val eggTrend: RiskLevel = RiskLevel.GOOD,
    val temperatureStatus: RiskLevel = RiskLevel.GOOD,
    val densityStatus: RiskLevel = RiskLevel.GOOD,
    val problemCount: Int = 0,
    val lastTemperature: Float? = null,
    val lastHumidity: Float? = null,
    val eggProductionRate: Float = 0f,
    val sqftPerBird: Float = 0f,
    val stressBreakdown: StressBreakdown? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BirdVisionApplication).repository
    private val prefs = (application as BirdVisionApplication).prefs

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.allBehaviorLogs,
                repository.allEggLogs,
                repository.allTemperatureLogs,
                repository.allNightWatchLogs
            ) { behavior, eggs, temps, nights ->
                computeState(behavior, eggs, temps, nights)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun computeState(
        behavior: List<com.vision.birdvisionpr.data.db.entity.BehaviorLogEntity>,
        eggs: List<com.vision.birdvisionpr.data.db.entity.EggLogEntity>,
        temps: List<com.vision.birdvisionpr.data.db.entity.TemperatureLogEntity>,
        nights: List<com.vision.birdvisionpr.data.db.entity.NightWatchEntity>
    ): HomeState {
        val coopArea = prefs.getFloat(BirdVisionApplication.PREF_COOP_AREA, 0f)
        val birdCount = prefs.getInt(BirdVisionApplication.PREF_BIRD_COUNT, 0)
        val lightHours = prefs.getFloat(BirdVisionApplication.PREF_LIGHT_HOURS, 14f)
        val sqftPerBird = if (birdCount > 0 && coopArea > 0f) coopArea / birdCount else 0f

        val recentBehavior = behavior.take(20)
        val avgSeverity = if (recentBehavior.isNotEmpty())
            recentBehavior.map { it.severity }.average().toFloat() else 1f

        val activityLevel = when {
            avgSeverity < 2f -> RiskLevel.GOOD
            avgSeverity < 3f -> RiskLevel.MODERATE
            avgSeverity < 4f -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }

        val recentEggs = eggs.take(14)
        val eggRate = if (recentEggs.isNotEmpty() && recentEggs.first().totalBirds > 0)
            recentEggs.first().count.toFloat() / recentEggs.first().totalBirds else 0f

        val eggTrend = when {
            eggs.isEmpty() -> RiskLevel.MODERATE
            eggRate >= 0.8f -> RiskLevel.GOOD
            eggRate >= 0.6f -> RiskLevel.MODERATE
            eggRate >= 0.4f -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }

        val latestTemp = temps.firstOrNull()
        val tempResult = if (latestTemp != null)
            StressCalculator.analyzeTemperature(latestTemp.temperature, latestTemp.humidity)
        else null

        val densityResult = StressCalculator.analyzeDensity(coopArea, birdCount)

        val breakdown = StressCalculator.calculate(
            avgBehaviorSeverity = avgSeverity,
            temperature = latestTemp?.temperature ?: 20f,
            humidity = latestTemp?.humidity ?: 60f,
            sqftPerBird = sqftPerBird,
            eggProductionRate = eggRate
        )

        val problems = ProblemAnalyzer.analyzeProblems(behavior, eggs, temps, nights, sqftPerBird)
        val significantProblems = problems.count { it.riskLevel != RiskLevel.GOOD }

        return HomeState(
            stressIndex = breakdown.total,
            stressLevel = StressCalculator.riskLevelFromScore(breakdown.total),
            activityLevel = activityLevel,
            eggTrend = eggTrend,
            temperatureStatus = tempResult?.status ?: RiskLevel.MODERATE,
            densityStatus = densityResult.riskLevel,
            problemCount = significantProblems,
            lastTemperature = latestTemp?.temperature,
            lastHumidity = latestTemp?.humidity,
            eggProductionRate = eggRate,
            sqftPerBird = sqftPerBird,
            stressBreakdown = breakdown
        )
    }
}
