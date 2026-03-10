package com.vision.birdvisionpr.ui.screens.lighting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vision.birdvisionpr.domain.analyzer.StressCalculator
import com.vision.birdvisionpr.domain.model.LampType
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightingScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = (context.applicationContext as BirdVisionApplication).prefs

    var lightHours by remember {
        mutableFloatStateOf(prefs.getFloat(BirdVisionApplication.PREF_LIGHT_HOURS, 14f))
    }
    var selectedLamp by remember {
        mutableStateOf(prefs.getString(BirdVisionApplication.PREF_LAMP_TYPE, LampType.LED.name) ?: LampType.LED.name)
    }
    var lampExpanded by remember { mutableStateOf(false) }

    val recommendation = remember(lightHours, selectedLamp) {
        StressCalculator.analyzeLighting(lightHours, selectedLamp)
    }

    LaunchedEffect(lightHours, selectedLamp) {
        prefs.edit()
            .putFloat(BirdVisionApplication.PREF_LIGHT_HOURS, lightHours)
            .putString(BirdVisionApplication.PREF_LAMP_TYPE, selectedLamp)
            .apply()
    }

    val currentLampType = LampType.values().find { it.name == selectedLamp } ?: LampType.LED

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Lighting Planner") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionHeader(
                    title = "Lighting Configuration",
                    subtitle = "Optimal: 14–16 hours light / day"
                )

                LightingStatusCard(recommendation = recommendation)

                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Light Hours per Day", fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("☀️", fontSize = 20.sp)
                            Slider(
                                value = lightHours,
                                onValueChange = { lightHours = it },
                                valueRange = 0f..24f,
                                steps = 23,
                                modifier = Modifier.weight(1f)
                            )
                            Text("🌙", fontSize = 20.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0h", style = MaterialTheme.typography.labelSmall)
                            Surface(shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    "${"%.0f".format(lightHours)}h light / ${"%.0f".format(24f - lightHours)}h dark",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text("24h", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Lamp Type", fontWeight = FontWeight.SemiBold)
                        ExposedDropdownMenuBox(
                            expanded = lampExpanded,
                            onExpandedChange = { lampExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = "💡 ${currentLampType.label}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Lamp Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(lampExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = lampExpanded, onDismissRequest = { lampExpanded = false }) {
                                LampType.values().forEach { lamp ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(lamp.label, fontWeight = FontWeight.Medium)
                                                Text(lamp.description, style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = { selectedLamp = lamp.name; lampExpanded = false }
                                    )
                                }
                            }
                        }
                        Text(
                            currentLampType.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SectionHeader(title = "Recommendations")
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recommendation.tips.forEach { tip ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", modifier = Modifier.width(16.dp))
                                Text(tip, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                LightingScheduleCard(lightHours = lightHours)
            }
        }
    }
}

@Composable
private fun LightingStatusCard(recommendation: com.vision.birdvisionpr.domain.model.LightingRecommendation) {
    val gradColors = if (recommendation.isOptimal)
        listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
    else
        listOf(Color(0xFFE65100), Color(0xFFF57C00))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(gradColors))
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (recommendation.isOptimal) "✅" else "⚠️", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    if (recommendation.isOptimal) "Optimal Lighting" else "Adjust Lighting",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    recommendation.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.9f)
                )
                Text(
                    "Recommended: ${"%.0f".format(recommendation.recommendedHours)} hours/day",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.7f)
                )
            }
        }
    }
}

@Composable
private fun LightingScheduleCard(lightHours: Float) {
    val darkHours = 24f - lightHours
    val lightStart = 5
    val lightEnd = (lightStart + lightHours).toInt()

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Suggested Schedule", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScheduleItem("💡 Lights ON", "${lightStart}:00 AM")
                ScheduleItem("🌙 Lights OFF", "${lightEnd}:00 ${if (lightEnd >= 12) "PM" else "AM"}")
                ScheduleItem("Dark period", "${"%.0f".format(darkHours)} hours")
            }
            Text(
                "Note: Maintain consistent schedule daily. Sudden changes can stress birds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScheduleItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}
