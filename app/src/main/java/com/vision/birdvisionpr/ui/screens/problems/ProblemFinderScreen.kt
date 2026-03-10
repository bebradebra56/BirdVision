package com.vision.birdvisionpr.ui.screens.problems

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vision.birdvisionpr.domain.analyzer.ProblemAnalyzer
import com.vision.birdvisionpr.domain.model.ProblemEntry
import com.vision.birdvisionpr.domain.model.RiskLevel
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import com.vision.birdvisionpr.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemFinderScreen(
    behaviorViewModel: BehaviorViewModel,
    eggViewModel: EggViewModel,
    temperatureViewModel: TemperatureViewModel,
    nightWatchViewModel: NightWatchViewModel,
    onNavigateBack: () -> Unit
) {
    val behaviorLogs by behaviorViewModel.behaviorLogs.collectAsState()
    val eggLogs by eggViewModel.eggLogs.collectAsState()
    val tempLogs by temperatureViewModel.temperatureLogs.collectAsState()
    val nightLogs by nightWatchViewModel.nightWatchLogs.collectAsState()

    val context = LocalContext.current
    val prefs = (context.applicationContext as BirdVisionApplication).prefs
    val coopArea = prefs.getFloat(BirdVisionApplication.PREF_COOP_AREA, 0f)
    val birdCount = prefs.getInt(BirdVisionApplication.PREF_BIRD_COUNT, 0)
    val sqftPerBird = if (birdCount > 0 && coopArea > 0f) coopArea / birdCount else 0f

    val problems = remember(behaviorLogs, eggLogs, tempLogs, nightLogs) {
        ProblemAnalyzer.analyzeProblems(behaviorLogs, eggLogs, tempLogs, nightLogs, sqftPerBird)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Problem Finder")
                        Text("AI-powered analysis", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(0.8f))
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProblemSummaryBanner(problems = problems)
            }

            item {
                SectionHeader(
                    title = "Detected Issues",
                    subtitle = "Based on your logged observations"
                )
            }

            items(problems) { problem ->
                ProblemCard(problem = problem)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Analysis is based on the last 20 behavior logs, 14 egg records, and recent temperature/night readings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProblemSummaryBanner(problems: List<ProblemEntry>) {
    val criticalCount = problems.count { it.riskLevel == RiskLevel.CRITICAL }
    val highCount = problems.count { it.riskLevel == RiskLevel.HIGH }
    val allGood = problems.all { it.riskLevel == RiskLevel.GOOD }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (allGood) "✅" else "🔍", fontSize = 36.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    if (allGood) "All Clear!" else "Issues Found",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!allGood) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (criticalCount > 0) {
                            Surface(shape = RoundedCornerShape(20.dp), color = ColorCritical.copy(0.15f)) {
                                Text(
                                    "🚨 $criticalCount Critical",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall, color = ColorCritical
                                )
                            }
                        }
                        if (highCount > 0) {
                            Surface(shape = RoundedCornerShape(20.dp), color = ColorHigh.copy(0.15f)) {
                                Text(
                                    "⚠️ $highCount High",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall, color = ColorHigh
                                )
                            }
                        }
                    }
                } else {
                    Text("No significant issues detected in current data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ProblemCard(problem: ProblemEntry) {
    var expanded by remember { mutableStateOf(false) }
    val riskColor = when (problem.riskLevel) {
        RiskLevel.GOOD -> ColorGood
        RiskLevel.MODERATE -> ColorModerate
        RiskLevel.HIGH -> ColorHigh
        RiskLevel.CRITICAL -> ColorCritical
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(riskColor, RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        problem.title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiskBadge(level = problem.riskLevel)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                problem.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, start = 22.dp)
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (problem.causes.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))
                        Text("Possible Causes", fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                            color = ColorHigh)
                        problem.causes.forEach { cause ->
                            Row(modifier = Modifier.padding(top = 4.dp)) {
                                Text("•", modifier = Modifier.width(16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(cause, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (problem.solutions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Recommended Actions", fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                            color = ColorGood)
                        problem.solutions.forEach { solution ->
                            Row(modifier = Modifier.padding(top = 4.dp)) {
                                Text("✓", modifier = Modifier.width(20.dp), color = ColorGood,
                                    fontWeight = FontWeight.Bold)
                                Text(solution, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
