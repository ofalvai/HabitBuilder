package com.ofalvai.habittracker.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.ui.dashboard.AddHabitScreen
import com.ofalvai.habittracker.ui.dashboard.Dashboard
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailScreen
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailViewModel

class MainActivity : AppCompatActivity() {

    private val dashboardViewModel by viewModels<DashboardViewModel> {
        Dependencies.viewModelFactory
    }
    private val habitDetailViewModel by viewModels<HabitDetailViewModel> {
        Dependencies.viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HabitTrackerTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = { MainTopBar(onNewHabit = { navController.navigate(Screen.AddHabit.route) }) }
                ) {
                    NavHost(navController, startDestination = Screen.Dashboard.route) {
                        composable(Screen.Dashboard.route) { Dashboard(dashboardViewModel, navController) }
                        composable(Screen.AddHabit.route) { AddHabitScreen(dashboardViewModel, navController) }
                        composable(Screen.HabitDetails.route, arguments = Screen.HabitDetails.arguments) { backStackEntry ->  
                            HabitDetailScreen(
                                habitId = Screen.HabitDetails.idFrom(backStackEntry.arguments),
                                viewModel = habitDetailViewModel
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

