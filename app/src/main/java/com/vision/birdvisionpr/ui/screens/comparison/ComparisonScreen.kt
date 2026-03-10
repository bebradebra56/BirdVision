package com.vision.birdvisionpr.ui.screens.comparison

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.viewmodel.BehaviorViewModel
import com.vision.birdvisionpr.viewmodel.EggViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    behaviorViewModel: BehaviorViewModel,
    eggViewModel: EggViewModel,
    onNavigateBack: () -> Unit
) {
    val behaviorLogs by behaviorViewModel.behaviorLogs.collectAsState()
    val eggLogs by eggViewModel.eggLogs.collectAsState()

    val now = System.currentTimeMillis()
    val msPerDay = 86_400_000L

    val periods = listOf("Last 7 days", "Last 14 days", "Last 30 days")
    var selectedPeriodA by remember { mutableIntStateOf(0) }
    var selectedPeriodB by remember { mutableIntStateOf(1) }
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }

    fun periodMs(index: Int): Long = when (index) {
        0 -> 7 * msPerDay
        1 -> 14 * msPerDay
        else -> 30 * msPerDay
    }

    val periodAMs = periodMs(selectedPeriodA)
    val periodBMs = periodMs(selectedPeriodB)

    val behaviorA = behaviorLogs.filter { it.timestamp >= now - periodAMs }
    val behaviorB = behaviorLogs.filter { it.timestamp >= now - periodBMs && it.timestamp < now - periodAMs }
    val eggsA = eggLogs.filter { it.date >= now - periodAMs }
    val eggsB = eggLogs.filter { it.date >= now - periodBMs && it.date < now - periodAMs }

    val avgSevA = if (behaviorA.isNotEmpty()) behaviorA.map { it.severity }.average() else 0.0
    val avgSevB = if (behaviorB.isNotEmpty()) behaviorB.map { it.severity }.average() else 0.0
    val eggRateA = if (eggsA.isNotEmpty()) eggsA.map { it.count.toFloat() / it.totalBirds.coerceAtLeast(1) }.average() else 0.0
    val eggRateB = if (eggsB.isNotEmpty()) eggsB.map { it.count.toFloat() / it.totalBirds.coerceAtLeast(1) }.average() else 0.0

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Period Comparison") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(title = "Select Periods to Compare")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedA,
                        onExpandedChange = { expandedA = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "Period A: ${periods[selectedPeriodA]}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Period A") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedA) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expandedA, onDismissRequest = { expandedA = false }) {
                            periods.forEachIndexed { i, p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = { selectedPeriodA = i; expandedA = false })
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedB,
                        onExpandedChange = { expandedB = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "Period B: ${periods[selectedPeriodB]}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Period B") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedB) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expandedB, onDismissRequest = { expandedB = false }) {
                            periods.forEachIndexed { i, p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = { selectedPeriodB = i; expandedB = false })
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Comparison Results")
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ComparisonHeader()
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        ComparisonRow(
                            label = "🐔 Avg Behavior Severity",
                            valueA = if (behaviorA.isEmpty()) "No data" else "${"%.1f".format(avgSevA)}/5",
                            valueB = if (behaviorB.isEmpty()) "No data" else "${"%.1f".format(avgSevB)}/5",
                            colorA = severityColor(avgSevA),
                            colorB = severityColor(avgSevB),
                            trend = compareTrend(avgSevA, avgSevB, lowerIsBetter = true)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        ComparisonRow(
                            label = "📊 Behavior Events",
                            valueA = "${behaviorA.size} events",
                            valueB = "${behaviorB.size} events",
                            colorA = if (behaviorA.size <= behaviorB.size) ColorGood else ColorHigh,
                            colorB = if (behaviorB.size <= behaviorA.size) ColorGood else ColorHigh,
                            trend = compareTrend(behaviorA.size.toDouble(), behaviorB.size.toDouble(), lowerIsBetter = true)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        ComparisonRow(
                            label = "🥚 Avg Egg Rate",
                            valueA = if (eggsA.isEmpty()) "No data" else "${"%.0f".format(eggRateA * 100)}%",
                            valueB = if (eggsB.isEmpty()) "No data" else "${"%.0f".format(eggRateB * 100)}%",
                            colorA = eggRateColor(eggRateA),
                            colorB = eggRateColor(eggRateB),
                            trend = compareTrend(eggRateA, eggRateB, lowerIsBetter = false)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        ComparisonRow(
                            label = "🥚 Total Eggs",
                            valueA = "${eggsA.sumOf { it.count }}",
                            valueB = "${eggsB.sumOf { it.count }}",
                            colorA = MaterialTheme.colorScheme.primary,
                            colorB = MaterialTheme.colorScheme.secondary,
                            trend = compareTrend(eggsA.sumOf { it.count }.toDouble(), eggsB.sumOf { it.count }.toDouble(), lowerIsBetter = false)
                        )
                    }
                }
            }

            item {
                ComparisonInsights(
                    avgSevA = avgSevA, avgSevB = avgSevB,
                    eggRateA = eggRateA, eggRateB = eggRateB,
                    behaviorCountA = behaviorA.size, behaviorCountB = behaviorB.size,
                    periodAName = periods[selectedPeriodA], periodBName = periods[selectedPeriodB]
                )
            }
        }
    }
}

@Composable
private fun ComparisonHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Metric", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text("Period A", style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
        Text("Period B", style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
        Spacer(modifier = Modifier.width(20.dp))
    }
}

@Composable
private fun ComparisonRow(label: String, valueA: String, valueB: String, colorA: Color, colorB: Color, trend: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(valueA, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
            color = colorA, modifier = Modifier.width(70.dp))
        Text(valueB, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
            color = colorB, modifier = Modifier.width(70.dp))
        Text(trend, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(20.dp))
    }
}

@Composable
private fun ComparisonInsights(
    avgSevA: Double, avgSevB: Double,
    eggRateA: Double, eggRateB: Double,
    behaviorCountA: Int, behaviorCountB: Int,
    periodAName: String, periodBName: String
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Insights", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

            val insights = buildList {
                if (avgSevA > 0 && avgSevB > 0) {
                    if (avgSevA < avgSevB - 0.3) add("✅ Behavior has improved in $periodAName vs $periodBName")
                    else if (avgSevA > avgSevB + 0.3) add("⚠️ Behavior severity increased in $periodAName")
                    else add("→ Behavior severity is stable across both periods")
                }
                if (eggRateA > 0 && eggRateB > 0) {
                    if (eggRateA > eggRateB + 0.05) add("✅ Egg production improved in $periodAName")
                    else if (eggRateA < eggRateB - 0.05) add("⚠️ Egg production declined in $periodAName")
                    else add("→ Egg production stable across periods")
                }
                if (behaviorCountA > behaviorCountB * 1.5 && behaviorCountA > 3)
                    add("⚠️ More behavior events logged in $periodAName — investigate stress factors")
                if (isEmpty()) add("Log more data to generate meaningful comparisons")
            }

            insights.forEach { insight ->
                Text(insight, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun severityColor(sev: Double) = when {
    sev < 2.0 -> ColorGood
    sev < 3.0 -> ColorModerate
    sev < 4.0 -> ColorHigh
    else -> ColorCritical
}

private fun eggRateColor(rate: Double) = when {
    rate >= 0.8 -> ColorGood
    rate >= 0.6 -> ColorModerate
    rate >= 0.4 -> ColorHigh
    else -> ColorCritical
}

private fun compareTrend(a: Double, b: Double, lowerIsBetter: Boolean): String {
    if (a == 0.0 || b == 0.0) return ""
    return if (lowerIsBetter) {
        if (a < b - 0.1) "↓" else if (a > b + 0.1) "↑" else "="
    } else {
        if (a > b + 0.05) "↑" else if (a < b - 0.05) "↓" else "="
    }
}
