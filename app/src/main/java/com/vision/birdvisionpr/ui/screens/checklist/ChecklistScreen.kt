package com.vision.birdvisionpr.ui.screens.checklist

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
import com.vision.birdvisionpr.data.db.entity.ChecklistEntity
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.viewmodel.ChecklistViewModel

data class CheckItem(
    val label: String,
    val emoji: String,
    val description: String,
    val getter: (ChecklistEntity) -> Boolean,
    val setter: (ChecklistEntity, Boolean) -> ChecklistEntity
)

val checkItems = listOf(
    CheckItem("Fresh Water", "💧", "Clean and refill all waterers",
        { it.water }, { e, v -> e.copy(water = v) }),
    CheckItem("Feed Stocked", "🌾", "Check feed level, remove stale feed",
        { it.feed }, { e, v -> e.copy(feed = v) }),
    CheckItem("Ventilation", "🌬️", "Ensure proper airflow, check vents",
        { it.ventilation }, { e, v -> e.copy(ventilation = v) }),
    CheckItem("Cleanliness", "🧹", "Remove manure, replace wet bedding",
        { it.cleanliness }, { e, v -> e.copy(cleanliness = v) }),
    CheckItem("Nest Boxes", "🪹", "Clean boxes, collect any hidden eggs",
        { it.nestBoxes }, { e, v -> e.copy(nestBoxes = v) }),
    CheckItem("Lighting", "💡", "Check light timer, replace bulbs if needed",
        { it.lighting }, { e, v -> e.copy(lighting = v) }),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(viewModel: ChecklistViewModel, onNavigateBack: () -> Unit) {
    val allChecklists by viewModel.allChecklists.collectAsState()
    var todayChecklist by remember { mutableStateOf<ChecklistEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getOrCreateTodayChecklist { checklist ->
            todayChecklist = checklist
        }
    }

    val completedCount = todayChecklist?.let { cl ->
        checkItems.count { it.getter(cl) }
    } ?: 0
    val totalCount = checkItems.size
    val allDone = completedCount == totalCount

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Daily Coop Check") },
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
                ChecklistProgressCard(
                    completed = completedCount,
                    total = totalCount,
                    allDone = allDone
                )
            }

            item { SectionHeader(title = "Today's Checklist") }

            todayChecklist?.let { cl ->
                items(checkItems) { item ->
                    ChecklistItem(
                        item = item,
                        checked = item.getter(cl),
                        onToggle = { checked ->
                            val updated = item.setter(cl, checked)
                            todayChecklist = updated
                            viewModel.updateChecklist(updated)
                        }
                    )
                }

                item {
                    ChecklistNotes(
                        notes = cl.notes,
                        onNotesChange = { notes ->
                            val updated = cl.copy(notes = notes)
                            todayChecklist = updated
                            viewModel.updateChecklist(updated)
                        }
                    )
                }
            } ?: run {
                item { CircularProgressIndicator(modifier = Modifier.padding(32.dp)) }
            }

            if (allChecklists.size > 1) {
                item { SectionHeader(title = "Previous Checklists") }
                items(allChecklists.drop(1).take(7)) { checklist ->
                    PastChecklistItem(checklist = checklist)
                }
            }
        }
    }
}

@Composable
private fun ChecklistProgressCard(completed: Int, total: Int, allDone: Boolean) {
    val progress = completed.toFloat() / total
    val gradColors = if (allDone)
        listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
    else
        listOf(Color(0xFF795548), Color(0xFFA1887F))

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
                    Text(
                        if (allDone) "All done! 🎉" else "In Progress",
                        color = Color.White, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "$completed / $total checks completed",
                        color = Color.White.copy(0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        formatDate(System.currentTimeMillis()),
                        color = Color.White.copy(0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    if (allDone) "✅" else "📋",
                    fontSize = 36.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = if (allDone) Color.White else Color(0xFFFFD54F),
                trackColor = Color.White.copy(0.3f)
            )
        }
    }
}

@Composable
private fun ChecklistItem(item: CheckItem, checked: Boolean, onToggle: (Boolean) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (checked)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.label,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (checked)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (checked)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Checkbox(
                checked = checked,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ChecklistNotes(notes: String, onNotesChange: (String) -> Unit) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Daily notes (optional)") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3,
        leadingIcon = { Text("📝") }
    )
}

@Composable
private fun PastChecklistItem(checklist: ChecklistEntity) {
    val completed = checkItems.count { it.getter(checklist) }
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (completed == checkItems.size) "✅" else "📋", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(formatDate(checklist.date), fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium)
                    if (checklist.notes.isNotEmpty()) {
                        Text(checklist.notes, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Surface(shape = RoundedCornerShape(20.dp),
                color = if (completed == checkItems.size) ColorGood.copy(0.15f)
                else ColorModerate.copy(0.15f)) {
                Text(
                    "$completed/${checkItems.size}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (completed == checkItems.size) ColorGood else ColorModerate
                )
            }
        }
    }
}
