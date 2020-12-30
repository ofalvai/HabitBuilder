package com.ofalvai.habittracker.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.ui.dashboard.AddHabitScreen
import com.ofalvai.habittracker.ui.dashboard.Dashboard
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailScreen

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<HabitViewModel> { Dependencies.viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HabitTrackerTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = { MainTopBar(onNewHabit = { navController.navigate(Screen.AddHabit.route) }) },
                    bottomBar = {
                        AppBottomNavigation(
                            onDashboardSelected = { navController.navigate(Screen.Dashboard.route) },
                            onInsightsSelected = { navController.navigate(Screen.Insights.route) }
                        )
                    }
                ) {
                    NavHost(navController, startDestination = Screen.Dashboard.route) {
                        composable(Screen.Dashboard.route) { Dashboard(viewModel, navController) }
                        composable(Screen.Insights.route) { Text("Work In Progress") }
                        composable(Screen.AddHabit.route) {
                            AddHabitScreen(
                                viewModel,
                                navController
                            )
                        }
                        composable(
                            Screen.HabitDetails.route,
                            arguments = Screen.HabitDetails.arguments
                        ) { backStackEntry ->
                            HabitDetailScreen(
                                habitId = Screen.HabitDetails.idFrom(backStackEntry.arguments),
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainTopBar(onNewHabit: () -> Unit) {
    TopAppBar(
        title = { Text("Habit Builder") },
        actions = {
            IconButton(onClick = onNewHabit) {
                Icon(Icons.Filled.Add)
            }
        }
    )
}

@Composable
fun ContentWithPlaceholder(
    showPlaceholder: Boolean,
    placeholder: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    if (showPlaceholder) {
        placeholder()
    } else {
        content()
    }
}

@Composable
fun AppBottomNavigation(
    onDashboardSelected: () -> Unit,
    onInsightsSelected: () -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }


    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.CheckCircle) },
            selected = selectedIndex == 0,
            onClick = {
                selectedIndex = 0
                onDashboardSelected()
            },
            label = { Text("Dashboard") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.DateRange) },
            selected = selectedIndex == 1,
            onClick = {
                selectedIndex = 1
                onInsightsSelected()
            },
            label = { Text("Insights") }
        )
    }
}