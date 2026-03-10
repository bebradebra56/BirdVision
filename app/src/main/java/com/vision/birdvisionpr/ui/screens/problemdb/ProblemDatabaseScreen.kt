package com.vision.birdvisionpr.ui.screens.problemdb

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
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
import androidx.navigation.NavController
import com.vision.birdvisionpr.domain.analyzer.ProblemDatabase
import com.vision.birdvisionpr.domain.model.ProblemEntry
import com.vision.birdvisionpr.domain.model.RiskLevel
import com.vision.birdvisionpr.ui.components.*
import com.vision.birdvisionpr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDatabaseScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<RiskLevel?>(null) }

    val allProblems = ProblemDatabase.allProblems
    val filtered = remember(searchQuery, selectedFilter) {
        ProblemDatabase.search(searchQuery).filter {
            selectedFilter == null || it.riskLevel == selectedFilter
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Problem Database")
                        Text("Chicken health knowledge base",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(0.8f))
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search problems...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${allProblems.size})") }
                    )
                    RiskLevel.values().filter { it != RiskLevel.GOOD }.forEach { level ->
                        val count = allProblems.count { it.riskLevel == level }
                        FilterChip(
                            selected = selectedFilter == level,
                            onClick = { selectedFilter = if (selectedFilter == level) null else level },
                            label = { Text("${level.label} ($count)") }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateMessage("🔍", "No results found", "Try a different search term")
                }
            }

            items(filtered) { problem ->
                ProblemDBCard(problem = problem)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "💡 Tap any card to expand details. Information is for reference — always consult a vet for diagnosis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProblemDBCard(problem: ProblemEntry) {
    var expanded by remember { mutableStateOf(false) }
    val riskColor = when (problem.riskLevel) {
        RiskLevel.GOOD -> ColorGood
        RiskLevel.MODERATE -> ColorModerate
        RiskLevel.HIGH -> ColorHigh
        RiskLevel.CRITICAL -> ColorCritical
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        problem.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    RiskBadge(level = problem.riskLevel)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                problem.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    if (problem.causes.isNotEmpty()) {
                        SectionBlock(
                            title = "Causes",
                            icon = "🔍",
                            items = problem.causes,
                            color = ColorHigh
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (problem.solutions.isNotEmpty()) {
                        SectionBlock(
                            title = "Solutions",
                            icon = "✅",
                            items = problem.solutions,
                            color = ColorGood
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    icon: String,
    items: List<String>,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        items.forEach { item ->
            Row(
                modifier = Modifier.padding(top = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(5.dp)
                        .background(color, RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(item, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
