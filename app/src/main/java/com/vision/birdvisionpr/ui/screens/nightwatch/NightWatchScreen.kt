package com.vision.birdvisionpr.ui.screens.nightwatch

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vision.birdvisionpr.data.db.entity.NightWatchEntity
import com.vision.birdvisionpr.domain.model.NightEventType
import com.vision.birdvisionpr.domain.model.RiskLevel
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.viewmodel.NightWatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NightWatchScreen(viewModel: NightWatchViewModel, onNavigateBack: () -> Unit) {
    val logs by viewModel.nightWatchLogs.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val panicCount = logs.take(7).count { it.eventType == "PANIC" || it.eventType == "PREDATOR_ALERT" }
    val showPredatorWarning = panicCount >= 2

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Night Watch") },
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
                text = { Text("Log Event") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showPredatorWarning) {
                item { PredatorWarningCard() }
            }

            item {
                NightSummaryCard(logs = logs)
            }

            if (logs.isEmpty()) {
                item {
                    EmptyStateMessage("🌙", "No night events logged", "Tap + to record a nighttime disturbance")
                }
            } else {
                item { SectionHeader(title = "Night Events (${logs.size})") }
                items(logs, key = { it.id }) { entry ->
                    NightWatchItem(entry = entry, onDelete = { viewModel.deleteEntry(entry) })
                }
            }
        }
    }

    if (showDialog) {
        AddNightEventDialog(
            onDismiss = { showDialog = false },
            onConfirm = { type, severity, notes ->
                viewModel.addEntry(type, severity, notes)
                showDialog = false
            }
        )
    }
}

@Composable
private fun PredatorWarningCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCritical.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("🦊", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Possible Predator Activity",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = ColorCritical
                )
                Text(
                    "Multiple panic events detected this week. Check for entry points, reinforce latches, and inspect the coop perimeter at night.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun NightSummaryCard(logs: List<NightWatchEntity>) {
    val thisWeek = logs.take(7)
    val noiseCount = thisWeek.count { it.eventType == "NOISE" }
    val panicCount = thisWeek.count { it.eventType == "PANIC" }
    val restlessCount = thisWeek.count { it.eventType == "RESTLESSNESS" }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This Week's Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                NightStatItem("🔊", noiseCount.toString(), "Noise")
                NightStatItem("😱", panicCount.toString(), "Panic")
                NightStatItem("🌀", restlessCount.toString(), "Restless")
                NightStatItem("📊", thisWeek.size.toString(), "Total")
            }
        }
    }
}

@Composable
private fun NightStatItem(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NightWatchItem(entry: NightWatchEntity, onDelete: () -> Unit) {
    val type = NightEventType.values().find { it.name == entry.eventType }
    val riskLevel = when (entry.severity) {
        in 1..2 -> RiskLevel.MODERATE
        3 -> RiskLevel.HIGH
        else -> RiskLevel.CRITICAL
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
                Text(type?.emoji ?: "🌙", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        type?.label ?: entry.eventType,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Severity ${entry.severity}/5 · ${formatTimestamp(entry.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.notes.isNotEmpty()) {
                        Text(entry.notes, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RiskBadge(level = riskLevel)
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Delete",
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNightEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(NightEventType.NOISE) }
    var severity by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Night Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedType.emoji} ${selectedType.label}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Event Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        NightEventType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text("${type.emoji} ${type.label}") },
                                onClick = { selectedType = type; typeExpanded = false }
                            )
                        }
                    }
                }

                SeveritySlider(value = severity, onValueChange = { severity = it })

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedType.name, severity, notes) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
