package com.vision.birdvisionpr.ui.screens.advisor

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
import androidx.navigation.NavController
import com.vision.birdvisionpr.domain.analyzer.ProblemAnalyzer
import com.vision.birdvisionpr.domain.model.Recommendation
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.navigation.Screen
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import com.vision.birdvisionpr.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisorScreen(
    behaviorViewModel: BehaviorViewModel,
    eggViewModel: EggViewModel,
    temperatureViewModel: TemperatureViewModel,
    nightWatchViewModel: NightWatchViewModel,
    navController: NavController
) {
    val behaviorLogs by behaviorViewModel.behaviorLogs.collectAsState()
    val eggLogs by eggViewModel.eggLogs.collectAsState()
    val tempLogs by temperatureViewModel.temperatureLogs.collectAsState()
    val nightLogs by nightWatchViewModel.nightWatchLogs.collectAsState()

    val context = LocalContext.current
    val prefs = (context.applicationContext as BirdVisionApplication).prefs
    val coopArea = prefs.getFloat(BirdVisionApplication.PREF_COOP_AREA, 0f)
    val birdCount = prefs.getInt(BirdVisionApplication.PREF_BIRD_COUNT, 0)
    val lightHours = prefs.getFloat(BirdVisionApplication.PREF_LIGHT_HOURS, 14f)
    val sqftPerBird = if (birdCount > 0 && coopArea > 0f) coopArea / birdCount else 0f
    val avgTemp = tempLogs.take(3).map { it.temperature }.average().let {
        if (tempLogs.isEmpty()) 20.0 else it
    }.toFloat()

    val problems = remember(behaviorLogs, eggLogs, tempLogs, nightLogs) {
        ProblemAnalyzer.analyzeProblems(behaviorLogs, eggLogs, tempLogs, nightLogs, sqftPerBird)
    }
    val recommendations = remember(problems, sqftPerBird, lightHours, avgTemp) {
        ProblemAnalyzer.generateRecommendations(problems, sqftPerBird, lightHours, avgTemp)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Advisor")
                        Text("Smart recommendations", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(0.8f))
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                AdvisorHeader(totalRecs = recommendations.size, urgentRecs = recommendations.count { it.priority == 1 })
            }

            item {
                SectionHeader(title = "Priority Actions")
            }

            val urgent = recommendations.filter { it.priority == 1 }
            val routine = recommendations.filter { it.priority > 1 }

            if (urgent.isNotEmpty()) {
                items(urgent) { rec -> RecommendationCard(rec = rec, isUrgent = true) }
            }

            if (routine.isNotEmpty()) {
                item { SectionHeader(title = "Routine Improvements") }
                items(routine) { rec -> RecommendationCard(rec = rec, isUrgent = false) }
            }

            item {
                SectionHeader(title = "Quick Navigation")
            }

            item {
                QuickNavGrid(navController = navController)
            }
        }
    }
}

@Composable
private fun AdvisorHeader(totalRecs: Int, urgentRecs: Int) {
    GradientCard(
        gradient = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("💡", fontSize = 36.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Your Advisor Report",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "$totalRecs recommendations found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f)
                )
                if (urgentRecs > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = ColorCritical.copy(0.2f)) {
                        Text(
                            "🚨 $urgentRecs urgent action${if (urgentRecs > 1) "s" else ""}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ColorCritical
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(rec: Recommendation, isUrgent: Boolean) {
    val borderColor = if (isUrgent) ColorCritical else MaterialTheme.colorScheme.surfaceVariant

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isUrgent) ColorCritical.copy(0.12f) else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    rec.icon,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        rec.title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (isUrgent) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ColorCritical.copy(0.15f)
                        ) {
                            Text(
                                "Urgent",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ColorCritical
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    rec.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickNavGrid(navController: NavController) {
    val navItems = listOf(
        Triple("🐔", "Behavior Log", Screen.Behavior.route),
        Triple("🥚", "Egg Pattern", Screen.Eggs.route),
        Triple("🌡️", "Temperature", Screen.Temperature.route),
        Triple("🏠", "Density", Screen.Density.route),
        Triple("💡", "Lighting", Screen.Lighting.route),
        Triple("🔍", "Problems", Screen.Problems.route),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        navItems.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (emoji, label, route) ->
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate(route) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(emoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
