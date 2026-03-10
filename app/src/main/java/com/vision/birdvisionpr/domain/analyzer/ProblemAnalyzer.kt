package com.vision.birdvisionpr.domain.analyzer

import com.vision.birdvisionpr.data.db.entity.*
import com.vision.birdvisionpr.domain.model.ProblemEntry
import com.vision.birdvisionpr.domain.model.Recommendation
import com.vision.birdvisionpr.domain.model.RiskLevel

object ProblemAnalyzer {

    fun analyzeProblems(
        behaviorLogs: List<BehaviorLogEntity>,
        eggLogs: List<EggLogEntity>,
        temperatureLogs: List<TemperatureLogEntity>,
        nightLogs: List<NightWatchEntity>,
        sqftPerBird: Float
    ): List<ProblemEntry> {
        val problems = mutableListOf<ProblemEntry>()

        val recentBehavior = behaviorLogs.take(20)
        val fightingCount = recentBehavior.count { it.behaviorType == "FIGHTING" }
        val aggressionCount = recentBehavior.count { it.behaviorType == "AGGRESSION" }
        val isolationCount = recentBehavior.count { it.behaviorType == "ISOLATION" }
        val apathyCount = recentBehavior.count { it.behaviorType == "APATHY" }
        val pecking = recentBehavior.count { it.behaviorType == "FEATHER_PECKING" }
        val avgSeverity = if (recentBehavior.isNotEmpty()) recentBehavior.map { it.severity }.average() else 0.0

        if (sqftPerBird in 0.01f..2.5f) {
            problems.add(
                ProblemEntry(
                    "Overcrowding Risk",
                    "Available space is below the recommended 4 sq ft per bird. This is a major cause of stress, aggression, and disease.",
                    listOf("Too many birds for available space", "Insufficient outdoor access", "No perch or nesting separation"),
                    listOf("Expand coop or reduce flock size", "Add outdoor run space", "Separate dominant and submissive birds"),
                    if (sqftPerBird < 1.5f) RiskLevel.CRITICAL else RiskLevel.HIGH
                )
            )
        }

        if (fightingCount >= 3 || aggressionCount >= 3) {
            problems.add(
                ProblemEntry(
                    "Flock Aggression",
                    "Multiple fighting and aggression events detected. This indicates dominance disputes or resource scarcity.",
                    listOf("Overcrowding", "Insufficient feeders/drinkers", "Mixing unfamiliar birds", "Feed deficiency (protein)"),
                    listOf("Add more feeding stations", "Separate aggressive individuals", "Provide environmental enrichment", "Review protein content in feed"),
                    RiskLevel.HIGH
                )
            )
        }

        if (isolationCount >= 2) {
            problems.add(
                ProblemEntry(
                    "Social Isolation Detected",
                    "Birds observed isolating themselves — a key sign of illness, injury, or extreme social stress.",
                    listOf("Illness or injury", "Bullying from dominant birds", "Severe stress", "Nutritional deficiency"),
                    listOf("Inspect isolated birds for injury or disease", "Create separate recovery space", "Review flock social dynamics"),
                    RiskLevel.HIGH
                )
            )
        }

        if (apathyCount >= 3) {
            problems.add(
                ProblemEntry(
                    "Lethargy / Apathy",
                    "Multiple birds showing signs of lethargy. This is often an early indicator of disease.",
                    listOf("Respiratory infection", "Parasites (mites, worms)", "Nutritional deficiency", "Poor ventilation / CO₂ buildup"),
                    listOf("Consult a vet for diagnosis", "Check for parasites", "Improve ventilation", "Review feed quality"),
                    RiskLevel.CRITICAL
                )
            )
        }

        if (pecking >= 2) {
            problems.add(
                ProblemEntry(
                    "Feather Pecking / Cannibalism Risk",
                    "Feather pecking behavior detected — can escalate to cannibalism if not addressed.",
                    listOf("Overcrowding", "Boredom / lack of enrichment", "Nutritional imbalance (salt/protein)", "Bright lighting stress"),
                    listOf("Add enrichment (pecking blocks, hung vegetables)", "Reduce light intensity", "Check feed sodium/protein levels", "Consider pinless peepers for aggressive birds"),
                    RiskLevel.HIGH
                )
            )
        }

        val recentEggs = eggLogs.take(14)
        if (recentEggs.size >= 7) {
            val firstHalf = recentEggs.takeLast(7).map { it.count.toFloat() / it.totalBirds }
            val secondHalf = recentEggs.take(7).map { it.count.toFloat() / it.totalBirds }
            val avgFirst = firstHalf.average()
            val avgSecond = secondHalf.average()
            if (avgSecond < avgFirst * 0.85 && avgSecond < 0.7) {
                problems.add(
                    ProblemEntry(
                        "Declining Egg Production",
                        "Egg production has dropped by more than 15% over the last 2 weeks.",
                        listOf("Stress from environment", "Nutritional deficiency", "Molting season", "Insufficient lighting", "Disease"),
                        listOf("Review lighting schedule (14–16 hrs)", "Check feed calcium and protein", "Reduce stress factors", "Consult vet if sudden drop"),
                        RiskLevel.HIGH
                    )
                )
            }
        }

        val recentTemp = temperatureLogs.take(3)
        if (recentTemp.isNotEmpty()) {
            val avgTemp = recentTemp.map { it.temperature }.average()
            if (avgTemp > 30f) {
                problems.add(
                    ProblemEntry(
                        "Heat Stress Risk",
                        "Recent temperatures exceed safe range. Heat stress significantly reduces productivity and can be fatal.",
                        listOf("High ambient temperature", "Poor ventilation", "Overcrowding generating body heat"),
                        listOf("Install additional ventilation", "Provide extra water access", "Add shade and cool zones", "Reduce flock density"),
                        if (avgTemp > 35f) RiskLevel.CRITICAL else RiskLevel.HIGH
                    )
                )
            } else if (avgTemp < 8f) {
                problems.add(
                    ProblemEntry(
                        "Cold Stress Risk",
                        "Temperatures are dangerously low for optimal chicken welfare.",
                        listOf("Insufficient insulation", "Cold drafts", "No supplemental heating"),
                        listOf("Add bedding depth (straw/shavings)", "Seal draft gaps", "Consider supplemental heating", "Ensure dry conditions"),
                        RiskLevel.HIGH
                    )
                )
            }
        }

        val recentNight = nightLogs.take(7)
        if (recentNight.count { it.eventType == "PANIC" || it.eventType == "PREDATOR_ALERT" } >= 2) {
            problems.add(
                ProblemEntry(
                    "Possible Predator Threat",
                    "Multiple night panic events suggest a predator is attempting to access the coop.",
                    listOf("Fox, raccoon, or weasel access points", "Loose latches or gaps in fencing", "Nocturnal rodents causing disturbance"),
                    listOf("Inspect coop perimeter at night", "Reinforce latches and hardware cloth", "Set up motion-activated light", "Consider guardian animal"),
                    RiskLevel.CRITICAL
                )
            )
        }

        if (problems.isEmpty()) {
            problems.add(
                ProblemEntry(
                    "No Significant Issues Detected",
                    "Based on current logs, your flock appears healthy. Keep logging observations regularly.",
                    emptyList(),
                    listOf("Maintain current management practices", "Continue daily observations", "Log any behavioral changes promptly"),
                    RiskLevel.GOOD
                )
            )
        }

        return problems.sortedByDescending { it.riskLevel.ordinal }
    }

    fun generateRecommendations(
        problems: List<ProblemEntry>,
        sqftPerBird: Float,
        lightingHours: Float,
        avgTemp: Float
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()

        if (sqftPerBird in 0.01f..3f) {
            recs.add(Recommendation("Increase Space", "Add at least ${String.format("%.1f", (4f - sqftPerBird))} sq ft per bird or reduce flock size.", 1, "🏠"))
        }
        if (lightingHours < 14f) {
            recs.add(Recommendation("Extend Lighting", "Increase daily light to 14–16 hours for optimal egg production.", 1, "💡"))
        }
        if (avgTemp > 28f) {
            recs.add(Recommendation("Improve Cooling", "Install fans or increase ventilation openings to combat heat stress.", 1, "🌬️"))
        }
        if (avgTemp < 10f) {
            recs.add(Recommendation("Add Warmth", "Add deep bedding and check for drafts to help birds stay warm.", 2, "🌡️"))
        }
        if (problems.any { it.title.contains("Aggression") || it.title.contains("Pecking") }) {
            recs.add(Recommendation("Add Enrichment", "Hang cabbages, add pecking blocks, and place dust bath areas to reduce boredom.", 2, "🎯"))
            recs.add(Recommendation("Check Protein Levels", "Review feed composition — low protein (below 16%) can trigger feather pecking.", 2, "🍖"))
        }
        if (problems.any { it.title.contains("Isolation") || it.title.contains("Lethargy") }) {
            recs.add(Recommendation("Vet Inspection", "Isolate affected birds and consult a veterinarian for diagnosis.", 1, "🩺"))
        }
        if (problems.any { it.title.contains("Egg Production") }) {
            recs.add(Recommendation("Review Nutrition", "Ensure 16–18% protein and adequate calcium (crushed oyster shell) in diet.", 2, "🥚"))
        }
        if (problems.any { it.title.contains("Predator") }) {
            recs.add(Recommendation("Secure the Coop", "Inspect and reinforce all entry points with hardware cloth. Check latches.", 1, "🔒"))
        }

        recs.add(Recommendation("Maintain Clean Water", "Refresh water at least twice daily. Dehydration causes rapid production drop.", 3, "💧"))
        recs.add(Recommendation("Daily Coop Inspection", "Walk through the coop every morning to spot early signs of illness or stress.", 3, "🔍"))
        recs.add(Recommendation("Clean Nest Boxes", "Clean nest boxes weekly to prevent egg-eating behavior and disease.", 3, "🪹"))

        return recs.sortedBy { it.priority }
    }
}
