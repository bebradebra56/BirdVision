package com.vision.birdvisionpr.domain.analyzer

import com.vision.birdvisionpr.domain.model.RiskLevel
import com.vision.birdvisionpr.domain.model.StressBreakdown
import kotlin.math.abs
import kotlin.math.min

object StressCalculator {

    private const val OPTIMAL_TEMP_C = 18f
    private const val OPTIMAL_SQFT_PER_BIRD = 4f

    fun calculate(
        avgBehaviorSeverity: Float,
        temperature: Float,
        humidity: Float,
        sqftPerBird: Float,
        eggProductionRate: Float
    ): StressBreakdown {
        val behaviorScore = ((avgBehaviorSeverity - 1f).coerceAtLeast(0f) / 4f) * 30f

        val tempDeviation = abs(temperature - OPTIMAL_TEMP_C)
        val tempScore = min(tempDeviation / 12f, 1f) * 25f

        val densityScore = when {
            sqftPerBird <= 0f -> 25f
            sqftPerBird >= OPTIMAL_SQFT_PER_BIRD -> 0f
            sqftPerBird > 2f -> ((OPTIMAL_SQFT_PER_BIRD - sqftPerBird) / 2f) * 15f
            else -> 25f
        }

        val eggScore = (1f - eggProductionRate.coerceIn(0f, 1f)) * 20f

        val total = (behaviorScore + tempScore + densityScore + eggScore).toInt().coerceIn(0, 100)

        return StressBreakdown(
            behaviorScore = behaviorScore,
            temperatureScore = tempScore,
            densityScore = densityScore,
            eggScore = eggScore,
            total = total
        )
    }

    fun riskLevelFromScore(score: Int): RiskLevel = when {
        score < 25 -> RiskLevel.GOOD
        score < 50 -> RiskLevel.MODERATE
        score < 75 -> RiskLevel.HIGH
        else -> RiskLevel.CRITICAL
    }

    fun analyzeTemperature(tempC: Float, humidity: Float): com.vision.birdvisionpr.domain.model.TemperatureResult {
        val heatIndex = tempC + (humidity / 100f) * 5f
        return when {
            tempC < 5f -> com.vision.birdvisionpr.domain.model.TemperatureResult(
                RiskLevel.CRITICAL, "Dangerously cold — risk of hypothermia and frostbite", heatIndex
            )
            tempC < 10f -> com.vision.birdvisionpr.domain.model.TemperatureResult(
                RiskLevel.HIGH, "Too cold — egg production will drop significantly", heatIndex
            )
            tempC in 10f..16f -> com.vision.birdvisionpr.domain.model.TemperatureResult(
                RiskLevel.MODERATE, "Slightly cool — consider supplemental heating", heatIndex
            )
            tempC in 16f..24f -> com.vision.birdvisionpr.domain.model.TemperatureResult(
                RiskLevel.GOOD, "Optimal temperature range for laying hens", heatIndex
            )
            tempC in 24f..29f -> com.vision.birdvisionpr.domain.model.TemperatureResult(
                RiskLevel.MODERATE, "Warm — ensure good ventilation and water access", heatIndex
            )
            tempC in 29f..35f -> com.vision.birdvisionpr.domain.model.TemperatureResult(
                RiskLevel.HIGH, "Hot — heat stress risk, increase ventilation urgently", heatIndex
            )
            else -> com.vision.birdvisionpr.domain.model.TemperatureResult(
                RiskLevel.CRITICAL, "Extreme heat — emergency cooling needed immediately", heatIndex
            )
        }
    }

    fun analyzeDensity(areaSqft: Float, birdCount: Int): com.vision.birdvisionpr.domain.model.DensityResult {
        if (birdCount <= 0 || areaSqft <= 0f) {
            return com.vision.birdvisionpr.domain.model.DensityResult(0f, RiskLevel.MODERATE, "Enter valid values to calculate")
        }
        val sqftPerBird = areaSqft / birdCount
        return when {
            sqftPerBird >= OPTIMAL_SQFT_PER_BIRD -> com.vision.birdvisionpr.domain.model.DensityResult(
                sqftPerBird, RiskLevel.GOOD,
                "Excellent — plenty of space per bird. Stress levels will be low."
            )
            sqftPerBird >= 3f -> com.vision.birdvisionpr.domain.model.DensityResult(
                sqftPerBird, RiskLevel.MODERATE,
                "Acceptable — slightly crowded. Monitor behavior for signs of stress."
            )
            sqftPerBird >= 2f -> com.vision.birdvisionpr.domain.model.DensityResult(
                sqftPerBird, RiskLevel.HIGH,
                "Crowded — increased risk of pecking, stress, and disease spread."
            )
            else -> com.vision.birdvisionpr.domain.model.DensityResult(
                sqftPerBird, RiskLevel.CRITICAL,
                "Severely overcrowded — immediate action required to reduce flock or expand space."
            )
        }
    }

    fun analyzeLighting(hoursPerDay: Float, lampType: String): com.vision.birdvisionpr.domain.model.LightingRecommendation {
        val recommendedHours = 16f
        val isOptimal = hoursPerDay in 14f..17f
        val tips = mutableListOf<String>()

        if (hoursPerDay < 14f) tips.add("Increase light to at least 14 hours for optimal production")
        if (hoursPerDay > 17f) tips.add("Too much light causes stress — aim for 14–16 hours")
        if (lampType == "INCANDESCENT") tips.add("Switch to LED for better efficiency and less heat")
        if (lampType == "LED" || lampType == "UV") tips.add("Great lamp choice — good for bird wellbeing")
        tips.add("Use consistent on/off schedule — avoid sudden light changes")
        tips.add("Dim light gradually in the evening to mimic natural sunset")

        val message = when {
            hoursPerDay < 10f -> "Very low light — significant drop in egg production expected"
            hoursPerDay < 14f -> "Below optimal — increase light exposure for better yields"
            isOptimal -> "Optimal lighting schedule — your hens should thrive"
            hoursPerDay > 17f -> "Too much light — may cause stress and feather pecking"
            else -> "Slightly over recommended range"
        }

        return com.vision.birdvisionpr.domain.model.LightingRecommendation(isOptimal, hoursPerDay, recommendedHours, message, tips)
    }
}
