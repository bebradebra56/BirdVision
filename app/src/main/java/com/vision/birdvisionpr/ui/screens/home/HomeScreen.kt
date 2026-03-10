package com.vision.birdvisionpr.ui.screens.home

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
fun HomeScreen(viewModel: HomeViewModel, navController: NavController) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "🐔 BirdVision",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Coop Health Radar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Checklist.route) }) {
                        Icon(Icons.Default.Done, contentDescription = "Checklist",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { navController.navigate(Screen.Calendar.route) }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendar",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://birdvisiion.com/privacy-policy.html"))
                        context.startActivity(intent) }) {
                        Icon(Icons.Default.PrivacyTip, contentDescription = "Privacy Policy",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
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
                StressIndexBanner(
                    stressIndex = state.stressIndex,
                    stressLevel = state.stressLevel,
                    problemCount = state.problemCount,
                    onClick = { navController.navigate(Screen.Stress.route) }
                )
            }

            item {
                SectionHeader(title = "Health Indicators", subtitle = "Tap a card for details")
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusCard(
                            title = "Activity Level",
                            value = state.activityLevel.label,
                            subtitle = "Behavior patterns",
                            icon = "🐔",
                            riskLevel = state.activityLevel,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate(Screen.Behavior.route) }
                        )
                        StatusCard(
                            title = "Egg Productivity",
                            value = if (state.eggProductionRate > 0f)
                                "${(state.eggProductionRate * 100).toInt()}%" else "No data",
                            subtitle = "Production rate",
                            icon = "🥚",
                            riskLevel = state.eggTrend,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate(Screen.Eggs.route) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusCard(
                            title = "Stress Level",
                            value = "${state.stressIndex}/100",
                            subtitle = "Overall stress index",
                            icon = "🌡",
                            riskLevel = state.stressLevel,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate(Screen.Stress.route) }
                        )
                        StatusCard(
                            title = "Temperature",
                            value = if (state.lastTemperature != null)
                                "${"%.1f".format(state.lastTemperature)}°C" else "No data",
                            subtitle = state.temperatureStatus.label,
                            icon = "🌡️",
                            riskLevel = state.temperatureStatus,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate(Screen.Temperature.route) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusCard(
                            title = "Feed Efficiency",
                            value = if (state.eggProductionRate >= 0.75f) "Good" else if (state.eggProductionRate > 0) "Review" else "No data",
                            subtitle = "Based on production",
                            icon = "🍽",
                            riskLevel = if (state.eggProductionRate >= 0.75f) RiskLevel.GOOD
                            else if (state.eggProductionRate > 0.5f) RiskLevel.MODERATE
                            else if (state.eggProductionRate > 0f) RiskLevel.HIGH
                            else RiskLevel.MODERATE,
                            modifier = Modifier.weight(1f)
                        )
                        StatusCard(
                            title = "Coop Density",
                            value = if (state.sqftPerBird > 0f) "${"%.1f".format(state.sqftPerBird)} ft²" else "Not set",
                            subtitle = "Per bird",
                            icon = "💧",
                            riskLevel = state.densityStatus,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate(Screen.Density.route) }
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "Quick Actions")
            }

            item {
                QuickActionsGrid(navController = navController)
            }
        }
    }
}

@Composable
private fun StressIndexBanner(
    stressIndex: Int,
    stressLevel: RiskLevel,
    problemCount: Int,
    onClick: () -> Unit
) {
    val gradientColors = when (stressLevel) {
        RiskLevel.GOOD -> listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
        RiskLevel.MODERATE -> listOf(Color(0xFFE65100), Color(0xFFF57C00))
        RiskLevel.HIGH -> listOf(Color(0xFFBF360C), Color(0xFFD84315))
        RiskLevel.CRITICAL -> listOf(Color(0xFF7F0000), Color(0xFFB71C1C))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(gradientColors))
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StressGauge(score = stressIndex, size = 90.dp)
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = "Stress Index",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = stressLevel.label,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (problemCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "⚠️ $problemCount issue${if (problemCount > 1) "s" else ""} found",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "✓ No significant issues",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

data class QuickAction(val label: String, val icon: ImageVector, val route: String)

@Composable
private fun QuickActionsGrid(navController: NavController) {
    val actions = listOf(
        QuickAction("Log Behavior", Icons.Default.Pets, Screen.Behavior.route),
        QuickAction("Log Eggs", Icons.Default.Star, Screen.Eggs.route),
        QuickAction("Temperature", Icons.Default.Thermostat, Screen.Temperature.route),
        QuickAction("Night Watch", Icons.Default.DarkMode, Screen.NightWatch.route),
        QuickAction("Find Problems", Icons.Default.BugReport, Screen.Problems.route),
        QuickAction("Compare", Icons.Default.CompareArrows, Screen.Comparison.route),
        QuickAction("Density Calc", Icons.Default.Apps, Screen.Density.route),
        QuickAction("Lighting", Icons.Default.WbSunny, Screen.Lighting.route),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { action ->
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate(action.route) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.label,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = action.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
