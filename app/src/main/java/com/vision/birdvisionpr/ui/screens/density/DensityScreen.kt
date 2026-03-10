package com.vision.birdvisionpr.ui.screens.density

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
import com.vision.birdvisionpr.domain.analyzer.StressCalculator
import com.vision.birdvisionpr.domain.model.RiskLevel
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DensityScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = (context.applicationContext as BirdVisionApplication).prefs

    var areaText by remember {
        mutableStateOf(prefs.getFloat(BirdVisionApplication.PREF_COOP_AREA, 0f).let {
            if (it > 0f) it.toString() else ""
        })
    }
    var countText by remember {
        mutableStateOf(prefs.getInt(BirdVisionApplication.PREF_BIRD_COUNT, 0).let {
            if (it > 0) it.toString() else ""
        })
    }

    val area = areaText.toFloatOrNull() ?: 0f
    val count = countText.toIntOrNull() ?: 0
    val result = remember(area, count) { StressCalculator.analyzeDensity(area, count) }

    LaunchedEffect(area, count) {
        if (area > 0f) prefs.edit().putFloat(BirdVisionApplication.PREF_COOP_AREA, area).apply()
        if (count > 0) prefs.edit().putInt(BirdVisionApplication.PREF_BIRD_COUNT, count).apply()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Density Calculator") },
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
                    title = "Coop Density Calculator",
                    subtitle = "Optimal space: 4 sq ft per bird"
                )

                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = areaText,
                            onValueChange = { areaText = it },
                            label = { Text("Coop Area (sq ft)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Text("🏠") }
                        )
                        OutlinedTextField(
                            value = countText,
                            onValueChange = { if (it.all { c -> c.isDigit() }) countText = it },
                            label = { Text("Number of Birds") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Text("🐔") }
                        )
                    }
                }

                if (area > 0f && count > 0) {
                    DensityResultCard(result = result, area = area, count = count)
                }

                DensityGuideCard()
            }
        }
    }
}

@Composable
private fun DensityResultCard(
    result: com.vision.birdvisionpr.domain.model.DensityResult,
    area: Float, count: Int
) {
    val gradColors = when (result.riskLevel) {
        RiskLevel.GOOD -> listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
        RiskLevel.MODERATE -> listOf(Color(0xFFE65100), Color(0xFFF57C00))
        RiskLevel.HIGH -> listOf(Color(0xFFBF360C), Color(0xFFD84315))
        RiskLevel.CRITICAL -> listOf(Color(0xFF7F0000), Color(0xFFB71C1C))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(gradColors))
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Space per Bird", color = Color.White.copy(0.8f),
                        style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${"%.2f".format(result.sqftPerBird)} sq ft",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.2f)) {
                    Text(
                        result.riskLevel.label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(0.15f)) {
                Text(
                    result.message,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoPill("Area: ${"%.0f".format(area)} ft²")
                InfoPill("Birds: $count")
                if (result.sqftPerBird < 4f && count > 0) {
                    val needed = (4f * count - area).coerceAtLeast(0f)
                    InfoPill("Need: +${"%.0f".format(needed)} ft²")
                }
            }
        }
    }
}

@Composable
private fun InfoPill(text: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(0.2f)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

@Composable
private fun DensityGuideCard() {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Space Standards", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            DensityRow("≥ 4 ft²/bird", "Excellent — low stress, high welfare", ColorGood)
            DensityRow("3–4 ft²/bird", "Acceptable — monitor behavior", ColorModerate)
            DensityRow("2–3 ft²/bird", "Crowded — stress risk increasing", ColorHigh)
            DensityRow("< 2 ft²/bird", "Overcrowded — immediate action needed", ColorCritical)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tip: Include outdoor run space in your calculation for free-range setups.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DensityRow(range: String, desc: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(50)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(range, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(90.dp))
        Text(desc, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
