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

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<DashboardViewModel> {
        Dependencies.dashboardViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HabitTrackerTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = { MainTopBar(onNewHabit = { navController.navigate(Routes.addHabit) }) }
                ) {
                    NavHost(navController, startDestination = Routes.dashboard) {
                        composable(Routes.dashboard) { Dashboard(viewModel) }
                        composable(Routes.addHabit) { AddHabitScreen(viewModel, navController) }
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

