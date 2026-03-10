package com.vision.birdvisionpr.ui.screens.behavior

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
import com.vision.birdvisionpr.data.db.entity.BehaviorLogEntity
import com.vision.birdvisionpr.domain.model.BehaviorType
import com.vision.birdvisionpr.domain.model.RiskLevel
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.viewmodel.BehaviorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorScreen(viewModel: BehaviorViewModel, onNavigateBack: () -> Unit) {
    val logs by viewModel.behaviorLogs.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Behavior Log") },
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
                text = { Text("Log Behavior") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateMessage(
                        icon = "🐔",
                        message = "No behavior logged yet",
                        sub = "Tap + to record an observation"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { SectionHeader(title = "Observations (${logs.size})") }
                    items(logs, key = { it.id }) { entry ->
                        BehaviorLogItem(
                            entry = entry,
                            onDelete = { viewModel.deleteEntry(entry) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddBehaviorDialog(
            onDismiss = { showDialog = false },
            onConfirm = { type, severity, count, notes ->
                viewModel.addEntry(type, severity, count, notes)
                showDialog = false
            }
        )
    }
}

@Composable
private fun BehaviorLogItem(entry: BehaviorLogEntity, onDelete: () -> Unit) {
    val type = BehaviorType.values().find { it.name == entry.behaviorType }
    val severityColor = when {
        entry.severity <= 2 -> ColorGood
        entry.severity == 3 -> ColorModerate
        entry.severity == 4 -> ColorHigh
        else -> ColorCritical
    }
    val riskLevel = when {
        entry.severity <= 2 -> RiskLevel.GOOD
        entry.severity == 3 -> RiskLevel.MODERATE
        entry.severity == 4 -> RiskLevel.HIGH
        else -> RiskLevel.CRITICAL
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = type?.emoji ?: "🐔", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = type?.label ?: entry.behaviorType,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = formatTimestamp(entry.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiskBadge(level = riskLevel)
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(label = "Severity", value = "${entry.severity}/5", color = severityColor)
                InfoChip(label = "Birds affected", value = "${entry.birdCount}", color = MaterialTheme.colorScheme.primary)
            }
            if (entry.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 ${entry.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.1f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBehaviorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(BehaviorType.FIGHTING) }
    var severity by remember { mutableIntStateOf(3) }
    var birdCount by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Behavior Observation") },
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
                        label = { Text("Behavior Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        BehaviorType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text("${type.emoji} ${type.label}") },
                                onClick = { selectedType = type; typeExpanded = false }
                            )
                        }
                    }
                }

                SeveritySlider(value = severity, onValueChange = { severity = it })

                OutlinedTextField(
                    value = birdCount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) birdCount = it },
                    label = { Text("Birds Affected") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
            Button(
                onClick = {
                    onConfirm(
                        selectedType.name,
                        severity,
                        birdCount.toIntOrNull() ?: 1,
                        notes
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
