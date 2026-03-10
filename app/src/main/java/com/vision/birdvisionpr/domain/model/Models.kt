package com.vision.birdvisionpr.domain.model

enum class BehaviorType(val label: String, val emoji: String) {
    FIGHTING("Fighting", "⚔️"),
    NOISE("Excessive Noise", "📢"),
    AGGRESSION("Aggression", "😡"),
    APATHY("Apathy / Lethargy", "😴"),
    ISOLATION("Isolation", "🚶"),
    FEATHER_PECKING("Feather Pecking", "🪶"),
    REDUCED_EATING("Reduced Eating", "🍽️")
}

enum class NightEventType(val label: String, val emoji: String) {
    NOISE("Night Noise", "🔊"),
    PANIC("Panic / Fright", "😱"),
    RESTLESSNESS("Restlessness", "🌀"),
    PREDATOR_ALERT("Predator Alert", "🦊")
}

enum class RiskLevel(val label: String) {
    GOOD("Good"),
    MODERATE("Moderate"),
    HIGH("High"),
    CRITICAL("Critical")
}

enum class LampType(val label: String, val description: String) {
    LED("LED", "Efficient, low heat, adjustable spectrum"),
    FLUORESCENT("Fluorescent", "Standard, moderate efficiency"),
    INCANDESCENT("Incandescent", "Warm light, higher heat output"),
    UV("UV / Full Spectrum", "Best for bird health & productivity")
}

data class StressBreakdown(
    val behaviorScore: Float,
    val temperatureScore: Float,
    val densityScore: Float,
    val eggScore: Float,
    val total: Int
)

data class ProblemEntry(
    val title: String,
    val description: String,
    val causes: List<String>,
    val solutions: List<String>,
    val riskLevel: RiskLevel
)

data class Recommendation(
    val title: String,
    val description: String,
    val priority: Int,
    val icon: String
)

data class DensityResult(
    val sqftPerBird: Float,
    val riskLevel: RiskLevel,
    val message: String
)

data class TemperatureResult(
    val status: RiskLevel,
    val message: String,
    val heatIndex: Float
)

data class LightingRecommendation(
    val isOptimal: Boolean,
    val currentHours: Float,
    val recommendedHours: Float,
    val message: String,
    val tips: List<String>
)
