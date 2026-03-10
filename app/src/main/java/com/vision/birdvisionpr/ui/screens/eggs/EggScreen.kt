package com.vision.birdvisionpr.ui.screens.eggs

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vision.birdvisionpr.data.db.entity.EggLogEntity
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.viewmodel.EggViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EggScreen(viewModel: EggViewModel, onNavigateBack: () -> Unit) {
    val logs by viewModel.eggLogs.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Egg Pattern") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
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
                text = { Text("Log Eggs") },
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
            if (logs.isNotEmpty()) {
                item {
                    EggSummaryCard(logs = logs)
                }
                item {
                    if (logs.size >= 3) {
                        EggChartCard(logs = logs.take(14).reversed())
                    }
                }
                item { SectionHeader(title = "Production History") }
                items(logs, key = { it.id }) { entry ->
                    EggLogItem(entry = entry, onDelete = { viewModel.deleteEntry(entry) })
                }
            } else {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateMessage(
                            icon = "🥚",
                            message = "No egg records yet",
                            sub = "Tap + to log today's egg count"
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEggDialog(
            onDismiss = { showDialog = false },
            onConfirm = { count, total, notes ->
                viewModel.addEntry(count, total, notes)
                showDialog = false
            }
        )
    }
}

@Composable
private fun EggSummaryCard(logs: List<EggLogEntity>) {
    val recent = logs.take(7)
    val avgRate = recent.map { it.count.toFloat() / it.totalBirds.coerceAtLeast(1) }.average().toFloat()
    val trend = if (logs.size >= 2) {
        val newest = logs.first().count.toFloat() / logs.first().totalBirds.coerceAtLeast(1)
        val older = logs[1].count.toFloat() / logs[1].totalBirds.coerceAtLeast(1)
        when {
            newest > older + 0.05f -> "↗ Improving"
            newest < older - 0.05f -> "↘ Declining"
            else -> "→ Stable"
        }
    } else "—"

    GradientCard(
        gradient = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("Today", if (logs.isNotEmpty()) "${logs.first().count}" else "—", "eggs")
            StatItem("7-day avg", "${(avgRate * 100).toInt()}%", "rate")
            StatItem("Trend", trend, "vs yesterday")
            StatItem("Total birds", "${logs.firstOrNull()?.totalBirds ?: "—"}", "hens")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f))
        Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.6f))
    }
}

@Composable
private fun EggChartCard(logs: List<EggLogEntity>) {
    val chartColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Production Chart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Last ${logs.size} entries", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)) {
                val rates = logs.map { it.count.toFloat() / it.totalBirds.coerceAtLeast(1) }
                val maxRate = rates.max().coerceAtLeast(0.1f)
                val stepX = if (rates.size > 1) size.width / (rates.size - 1) else size.width
                val points = rates.mapIndexed { i, r ->
                    Offset(x = i * stepX, y = size.height * (1f - r / maxRate))
                }

                // Grid lines
                repeat(4) { i ->
                    val y = size.height * i / 3
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                }

                // Filled area
                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, size.height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, size.height)
                        close()
                    }
                    drawPath(path, chartColor.copy(alpha = 0.15f))

                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(linePath, chartColor, style = Stroke(width = 3f))
                }

                points.forEach { point ->
                    drawCircle(chartColor, radius = 5f, center = point)
                    drawCircle(Color.White, radius = 3f, center = point)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    formatShortDate(logs.first().date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatShortDate(logs.last().date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EggLogItem(entry: EggLogEntity, onDelete: () -> Unit) {
    val rate = entry.count.toFloat() / entry.totalBirds.coerceAtLeast(1)
    val rateColor = when {
        rate >= 0.8f -> ColorGood
        rate >= 0.6f -> ColorModerate
        rate >= 0.4f -> ColorHigh
        else -> ColorCritical
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
                Text("🥚", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "${entry.count} eggs from ${entry.totalBirds} hens",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        formatDate(entry.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.notes.isNotEmpty()) {
                        Text(entry.notes, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${(rate * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = rateColor,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Delete",
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AddEggDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String) -> Unit
) {
    var count by remember { mutableStateOf("") }
    var totalBirds by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Egg Production") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = count,
                    onValueChange = { if (it.all { c -> c.isDigit() }) count = it },
                    label = { Text("Eggs Collected Today") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("🥚") }
                )
                OutlinedTextField(
                    value = totalBirds,
                    onValueChange = { if (it.all { c -> c.isDigit() }) totalBirds = it },
                    label = { Text("Total Laying Hens") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("🐔") }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = count.toIntOrNull() ?: 0
                    val t = totalBirds.toIntOrNull() ?: 1
                    if (c >= 0 && t > 0) onConfirm(c, t, notes)
                },
                enabled = count.isNotEmpty() && totalBirds.isNotEmpty()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
