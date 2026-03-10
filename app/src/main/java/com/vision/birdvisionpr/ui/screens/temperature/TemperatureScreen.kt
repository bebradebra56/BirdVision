package com.vision.birdvisionpr.ui.screens.temperature

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vision.birdvisionpr.data.db.entity.TemperatureLogEntity
import com.vision.birdvisionpr.domain.analyzer.StressCalculator
import com.vision.birdvisionpr.domain.model.RiskLevel
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.viewmodel.TemperatureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureScreen(viewModel: TemperatureViewModel, onNavigateBack: () -> Unit) {
    val logs by viewModel.temperatureLogs.collectAsState()
    val latest by viewModel.latestTemperature.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val currentResult = latest?.let {
        StressCalculator.analyzeTemperature(it.temperature, it.humidity)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Temperature Monitor") },
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Log Reading") },
                containerColor = MaterialTheme.colorScheme.primary
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
                if (latest != null && currentResult != null) {
                    TemperatureBanner(latest!!, currentResult.status, currentResult.message)
                } else {
                    EmptyStateMessage("🌡️", "No temperature data", "Tap + to log a reading")
                }
            }

            if (logs.isNotEmpty()) {
                item { SectionHeader(title = "Temperature Range Guide") }
                item { TemperatureRangeCard() }
                item { SectionHeader(title = "History") }
                items(logs, key = { it.id }) { entry ->
                    TemperatureLogItem(entry = entry, onDelete = { viewModel.deleteEntry(entry) })
                }
            }
        }
    }

    if (showDialog) {
        AddTemperatureDialog(
            onDismiss = { showDialog = false },
            onConfirm = { temp, hum ->
                viewModel.addEntry(temp, hum)
                showDialog = false
            }
        )
    }
}

@Composable
private fun TemperatureBanner(
    entry: TemperatureLogEntity,
    status: RiskLevel,
    message: String
) {
    val gradColors = when (status) {
        RiskLevel.GOOD -> listOf(Color(0xFF1B5E20), Color(0xFF388E3C))
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
                    Text("Current Temperature", color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${"%.1f".format(entry.temperature)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        )
                        Text("°C", color = Color.White.copy(alpha = 0.8f), fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Text("Humidity: ${"%.0f".format(entry.humidity)}%", color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium)
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            when (status) {
                                RiskLevel.GOOD -> "✓"
                                RiskLevel.MODERATE -> "!"
                                RiskLevel.HIGH -> "⚠"
                                RiskLevel.CRITICAL -> "🚨"
                            },
                            fontSize = 28.sp
                        )
                        Text(status.label, color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.15f)) {
                Text(
                    text = message,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Last updated: ${formatTimestamp(entry.timestamp)}",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun TemperatureRangeCard() {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Optimal Ranges", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            RangeRow("< 5°C", "Hypothermia risk", ColorCritical)
            RangeRow("5–10°C", "Too cold", ColorHigh)
            RangeRow("10–16°C", "Cool — monitor closely", ColorModerate)
            RangeRow("16–24°C", "✓ Optimal for laying", ColorGood)
            RangeRow("24–29°C", "Warm — watch hydration", ColorModerate)
            RangeRow("29–35°C", "Heat stress risk", ColorHigh)
            RangeRow("> 35°C", "Critical — emergency", ColorCritical)
        }
    }
}

@Composable
private fun RangeRow(range: String, label: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(range, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium,
            modifier = Modifier.width(70.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f))
        Box(modifier = Modifier
            .size(10.dp)
            .background(color, RoundedCornerShape(50)))
    }
}

@Composable
private fun TemperatureLogItem(entry: TemperatureLogEntity, onDelete: () -> Unit) {
    val result = StressCalculator.analyzeTemperature(entry.temperature, entry.humidity)
    val riskColor = when (result.status) {
        RiskLevel.GOOD -> ColorGood
        RiskLevel.MODERATE -> ColorModerate
        RiskLevel.HIGH -> ColorHigh
        RiskLevel.CRITICAL -> ColorCritical
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌡️", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "${"%.1f".format(entry.temperature)}°C · ${"%.0f".format(entry.humidity)}% hum.",
                        fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge
                    )
                    Text(formatTimestamp(entry.timestamp), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RiskBadge(level = result.status)
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Delete",
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AddTemperatureDialog(onDismiss: () -> Unit, onConfirm: (Float, Float) -> Unit) {
    var temp by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Temperature") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = temp,
                    onValueChange = { temp = it },
                    label = { Text("Temperature (°C)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("🌡️") }
                )
                OutlinedTextField(
                    value = humidity,
                    onValueChange = { humidity = it },
                    label = { Text("Humidity (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("💧") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = temp.toFloatOrNull()
                    val h = humidity.toFloatOrNull()
                    if (t != null && h != null) onConfirm(t, h)
                },
                enabled = temp.toFloatOrNull() != null && humidity.toFloatOrNull() != null
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
