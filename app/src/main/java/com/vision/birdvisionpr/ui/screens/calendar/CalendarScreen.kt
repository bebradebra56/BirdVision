package com.vision.birdvisionpr.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vision.birdvisionpr.data.db.entity.BehaviorLogEntity
import com.vision.birdvisionpr.data.db.entity.EggLogEntity
import com.vision.birdvisionpr.data.db.entity.TemperatureLogEntity
import com.vision.birdvisionpr.domain.model.BehaviorType
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*
import com.vision.birdvisionpr.viewmodel.BehaviorViewModel
import com.vision.birdvisionpr.viewmodel.EggViewModel
import com.vision.birdvisionpr.viewmodel.TemperatureViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    behaviorViewModel: BehaviorViewModel,
    eggViewModel: EggViewModel,
    temperatureViewModel: TemperatureViewModel,
    onNavigateBack: () -> Unit
) {
    val behaviorLogs by behaviorViewModel.behaviorLogs.collectAsState()
    val eggLogs by eggViewModel.eggLogs.collectAsState()
    val tempLogs by temperatureViewModel.temperatureLogs.collectAsState()

    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val cal = Calendar.getInstance().apply { set(selectedYear, selectedMonth, 1) }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1 + 6) % 7 // Mon=0

    fun dayKey(day: Int): Long {
        val c = Calendar.getInstance()
        c.set(selectedYear, selectedMonth, day, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun eventsOnDay(day: Int): List<String> {
        val start = dayKey(day)
        val end = start + 86_400_000L
        val events = mutableListOf<String>()
        if (behaviorLogs.any { it.timestamp in start until end }) events.add("🐔")
        if (eggLogs.any { it.date in start until end }) events.add("🥚")
        if (tempLogs.any { it.timestamp in start until end }) events.add("🌡️")
        return events
    }

    val selectedDayEvents: Triple<List<BehaviorLogEntity>, List<EggLogEntity>, List<TemperatureLogEntity>>? =
        selectedDay?.let { day ->
            val start = dayKey(day)
            val end = start + 86_400_000L
            Triple(
                behaviorLogs.filter { it.timestamp in start until end },
                eggLogs.filter { it.date in start until end },
                tempLogs.filter { it.timestamp in start until end }
            )
        }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Observation Calendar") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonthNavigator(
                    year = selectedYear,
                    month = selectedMonth,
                    onPrev = {
                        if (selectedMonth == 0) { selectedMonth = 11; selectedYear-- }
                        else selectedMonth--
                        selectedDay = null
                    },
                    onNext = {
                        if (selectedMonth == 11) { selectedMonth = 0; selectedYear++ }
                        else selectedMonth++
                        selectedDay = null
                    }
                )
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Day headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                                Text(
                                    d, textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar grid
                        val totalCells = firstDayOfWeek + daysInMonth
                        val rows = (totalCells + 6) / 7
                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 7) {
                                    val dayNum = row * 7 + col - firstDayOfWeek + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val events = eventsOnDay(dayNum)
                                        val isSelected = selectedDay == dayNum
                                        val isToday = dayNum == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                                                selectedMonth == Calendar.getInstance().get(Calendar.MONTH) &&
                                                selectedYear == Calendar.getInstance().get(Calendar.YEAR)

                                        CalendarDay(
                                            day = dayNum,
                                            events = events,
                                            isSelected = isSelected,
                                            isToday = isToday,
                                            modifier = Modifier.weight(1f),
                                            onClick = { selectedDay = if (selectedDay == dayNum) null else dayNum }
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedDay != null && selectedDayEvents != null) {
                val (bLogs, eLogs, tLogs) = selectedDayEvents
                item {
                    SectionHeader(title = "Events on ${selectedDay} ${monthName(selectedMonth)} $selectedYear")
                }

                if (bLogs.isEmpty() && eLogs.isEmpty() && tLogs.isEmpty()) {
                    item { EmptyStateMessage("📅", "No events this day") }
                }

                items(bLogs) { entry ->
                    val type = BehaviorType.values().find { it.name == entry.behaviorType }
                    EventItem(
                        icon = type?.emoji ?: "🐔",
                        title = type?.label ?: entry.behaviorType,
                        subtitle = "Severity: ${entry.severity}/5 · ${entry.birdCount} birds",
                        time = formatTimestamp(entry.timestamp)
                    )
                }

                items(eLogs) { entry ->
                    EventItem(
                        icon = "🥚",
                        title = "${entry.count} eggs from ${entry.totalBirds} hens",
                        subtitle = "${"%.0f".format(entry.count.toFloat() / entry.totalBirds * 100)}% production rate",
                        time = formatDate(entry.date)
                    )
                }

                items(tLogs) { entry ->
                    EventItem(
                        icon = "🌡️",
                        title = "${"%.1f".format(entry.temperature)}°C · ${"%.0f".format(entry.humidity)}% humidity",
                        subtitle = "Temperature reading",
                        time = formatTimestamp(entry.timestamp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthNavigator(year: Int, month: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, "Previous")
        }
        Text(
            "${monthName(month)} $year",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, "Next")
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int, events: List<String>,
    isSelected: Boolean, isToday: Boolean,
    modifier: Modifier, onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .then(if (events.isNotEmpty() && !isSelected && !isToday)
                Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(0.3f), RoundedCornerShape(8.dp))
            else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$day",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (events.isNotEmpty()) {
                Row {
                    events.take(2).forEach { e ->
                        Text(e, fontSize = 7.sp, lineHeight = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EventItem(icon: String, title: String, subtitle: String, time: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(time, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun monthName(month: Int): String =
    SimpleDateFormat("MMMM", Locale.ENGLISH).format(Calendar.getInstance().apply { set(Calendar.MONTH, month) }.time)
