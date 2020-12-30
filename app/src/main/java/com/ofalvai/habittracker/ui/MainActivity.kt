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
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailScreen

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<HabitViewModel> { Dependencies.viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HabitTrackerTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = { MainTopBar(onNewHabit = { navController.navigate(Screen.AddHabit.route) }) }
                ) {
                    NavHost(navController, startDestination = Screen.Dashboard.route) {
                        composable(Screen.Dashboard.route) { Dashboard(viewModel, navController) }
                        composable(Screen.AddHabit.route) { AddHabitScreen(viewModel, navController) }
                        composable(Screen.HabitDetails.route, arguments = Screen.HabitDetails.arguments) { backStackEntry ->  
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

