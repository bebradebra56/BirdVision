package com.vision.birdvisionpr.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.vision.birdvisionpr.ui.screens.advisor.AdvisorScreen
import com.vision.birdvisionpr.ui.screens.behavior.BehaviorScreen
import com.vision.birdvisionpr.ui.screens.calendar.CalendarScreen
import com.vision.birdvisionpr.ui.screens.checklist.ChecklistScreen
import com.vision.birdvisionpr.ui.screens.comparison.ComparisonScreen
import com.vision.birdvisionpr.ui.screens.density.DensityScreen
import com.vision.birdvisionpr.ui.screens.eggs.EggScreen
import com.vision.birdvisionpr.ui.screens.home.HomeScreen
import com.vision.birdvisionpr.ui.screens.lighting.LightingScreen
import com.vision.birdvisionpr.ui.screens.nightwatch.NightWatchScreen
import com.vision.birdvisionpr.ui.screens.problems.ProblemFinderScreen
import com.vision.birdvisionpr.ui.screens.problemdb.ProblemDatabaseScreen
import com.vision.birdvisionpr.ui.screens.stress.StressIndexScreen
import com.vision.birdvisionpr.ui.screens.temperature.TemperatureScreen
import com.vision.birdvisionpr.viewmodel.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Radar", Icons.Default.Favorite)
    object Behavior : Screen("behavior", "Behavior", Icons.Default.Pets)
    object Eggs : Screen("eggs", "Eggs", Icons.Default.Star)
    object Temperature : Screen("temperature", "Temp", Icons.Default.Thermostat)
    object NightWatch : Screen("night_watch", "Night", Icons.Default.DarkMode)
    object Problems : Screen("problems", "Problems", Icons.Default.BugReport)
    object Stress : Screen("stress", "Stress", Icons.Default.FavoriteBorder)
    object Comparison : Screen("comparison", "Compare", Icons.Default.CompareArrows)
    object Density : Screen("density", "Density", Icons.Default.Apps)
    object Lighting : Screen("lighting", "Lighting", Icons.Default.WbSunny)
    object Advisor : Screen("advisor", "Advisor", Icons.Default.Lightbulb)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Checklist : Screen("checklist", "Checklist", Icons.Default.Done)
    object ProblemDB : Screen("problem_db", "Guide", Icons.Default.Book)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Behavior,
    Screen.Stress,
    Screen.Advisor,
    Screen.ProblemDB
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val vm: HomeViewModel = viewModel()
                HomeScreen(viewModel = vm, navController = navController)
            }
            composable(Screen.Behavior.route) {
                val vm: BehaviorViewModel = viewModel()
                BehaviorScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Eggs.route) {
                val vm: EggViewModel = viewModel()
                EggScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Temperature.route) {
                val vm: TemperatureViewModel = viewModel()
                TemperatureScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.NightWatch.route) {
                val vm: NightWatchViewModel = viewModel()
                NightWatchScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Problems.route) {
                val behaviorVm: BehaviorViewModel = viewModel()
                val eggVm: EggViewModel = viewModel()
                val tempVm: TemperatureViewModel = viewModel()
                val nightVm: NightWatchViewModel = viewModel()
                ProblemFinderScreen(
                    behaviorViewModel = behaviorVm,
                    eggViewModel = eggVm,
                    temperatureViewModel = tempVm,
                    nightWatchViewModel = nightVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Stress.route) {
                val homeVm: HomeViewModel = viewModel()
                StressIndexScreen(viewModel = homeVm, navController = navController)
            }
            composable(Screen.Comparison.route) {
                val behaviorVm: BehaviorViewModel = viewModel()
                val eggVm: EggViewModel = viewModel()
                ComparisonScreen(
                    behaviorViewModel = behaviorVm,
                    eggViewModel = eggVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Density.route) {
                DensityScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Lighting.route) {
                LightingScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Advisor.route) {
                val behaviorVm: BehaviorViewModel = viewModel()
                val eggVm: EggViewModel = viewModel()
                val tempVm: TemperatureViewModel = viewModel()
                val nightVm: NightWatchViewModel = viewModel()
                AdvisorScreen(
                    behaviorViewModel = behaviorVm,
                    eggViewModel = eggVm,
                    temperatureViewModel = tempVm,
                    nightWatchViewModel = nightVm,
                    navController = navController
                )
            }
            composable(Screen.Calendar.route) {
                val behaviorVm: BehaviorViewModel = viewModel()
                val eggVm: EggViewModel = viewModel()
                val tempVm: TemperatureViewModel = viewModel()
                CalendarScreen(
                    behaviorViewModel = behaviorVm,
                    eggViewModel = eggVm,
                    temperatureViewModel = tempVm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Checklist.route) {
                val vm: ChecklistViewModel = viewModel()
                ChecklistScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.ProblemDB.route) {
                ProblemDatabaseScreen(navController = navController)
            }
        }
    }
}
