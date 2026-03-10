package com.vision.birdvisionpr.ui.screens.stress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vision.birdvisionpr.domain.model.RiskLevel
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.navigation.Screen
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StressIndexScreen(viewModel: HomeViewModel, navController: NavController) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Stress Index") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StressMainCard(
                    score = state.stressIndex,
                    level = state.stressLevel
                )
            }

            item {
                SectionHeader(
                    title = "Stress Components",
                    subtitle = "Breakdown of contributing factors"
                )
            }

            item {
                state.stressBreakdown?.let { breakdown ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ProgressBar(
                                progress = breakdown.behaviorScore / 30f,
                                color = colorForScore(breakdown.behaviorScore, 30f),
                                label = "🐔 Behavior",
                                value = "${"%.0f".format(breakdown.behaviorScore)}/30"
                            )
                            ProgressBar(
                                progress = breakdown.temperatureScore / 25f,
                                color = colorForScore(breakdown.temperatureScore, 25f),
                                label = "🌡️ Temperature",
                                value = "${"%.0f".format(breakdown.temperatureScore)}/25"
                            )
                            ProgressBar(
                                progress = breakdown.densityScore / 25f,
                                color = colorForScore(breakdown.densityScore, 25f),
                                label = "🏠 Density",
                                value = "${"%.0f".format(breakdown.densityScore)}/25"
                            )
                            ProgressBar(
                                progress = breakdown.eggScore / 20f,
                                color = colorForScore(breakdown.eggScore, 20f),
                                label = "🥚 Egg Production",
                                value = "${"%.0f".format(breakdown.eggScore)}/20"
                            )

                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Stress Index", fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${breakdown.total}/100",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = when (state.stressLevel) {
                                        RiskLevel.GOOD -> ColorGood
                                        RiskLevel.MODERATE -> ColorModerate
                                        RiskLevel.HIGH -> ColorHigh
                                        RiskLevel.CRITICAL -> ColorCritical
                                    }
                                )
                            }
                        }
                    }
                } ?: ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    EmptyStateMessage("📊", "Insufficient data", "Log behaviors, temperatures, and egg counts to see breakdown")
                }
            }

            item {
                SectionHeader(title = "Risk Scale Reference")
                RiskScaleCard()
            }

            item {
                SectionHeader(title = "Quick Log")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickLogButton(
                        icon = "🐔", label = "Behavior",
                        onClick = { navController.navigate(Screen.Behavior.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickLogButton(
                        icon = "🌡️", label = "Temperature",
                        onClick = { navController.navigate(Screen.Temperature.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickLogButton(
                        icon = "🥚", label = "Eggs",
                        onClick = { navController.navigate(Screen.Eggs.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun colorForScore(score: Float, max: Float) =
    when {
        score / max < 0.33f -> ColorGood
        score / max < 0.66f -> ColorModerate
        score / max < 0.85f -> ColorHigh
        else -> ColorCritical
    }

@Composable
private fun StressMainCard(score: Int, level: RiskLevel) {
    val color = when (level) {
        RiskLevel.GOOD -> ColorGood
        RiskLevel.MODERATE -> ColorModerate
        RiskLevel.HIGH -> ColorHigh
        RiskLevel.CRITICAL -> ColorCritical
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(6.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Overall Flock Stress", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            StressGauge(score = score, size = 160.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.15f)) {
                Text(
                    text = level.label,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                when (level) {
                    RiskLevel.GOOD -> "Your flock is in great shape! Keep up the good work."
                    RiskLevel.MODERATE -> "Some factors need attention. Review the breakdown below."
                    RiskLevel.HIGH -> "Significant stress detected. Take action soon."
                    RiskLevel.CRITICAL -> "Critical stress levels! Immediate intervention needed."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RiskScaleCard() {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RiskScaleRow("0–24", "Good — optimal conditions", ColorGood)
            RiskScaleRow("25–49", "Moderate — minor concerns", ColorModerate)
            RiskScaleRow("50–74", "High — action needed", ColorHigh)
            RiskScaleRow("75–100", "Critical — immediate intervention", ColorCritical)
        }
    }
}

@Composable
private fun RiskScaleRow(range: String, desc: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(12.dp)
            .background(color, RoundedCornerShape(50)))
        Spacer(modifier = Modifier.width(10.dp))
        Text(range, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(55.dp))
        Text(desc, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickLogButton(icon: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
