package com.ofalvai.habittracker.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.setContent
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.ui.composable.HabitCard
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<DashboardViewModel> {
        Dependencies.dashboardViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.habitsWithActions.observe(this) {
            Timber.d("Habits: $it")
        }

        val onActionToggle: (Action, Habit, Int) -> Unit = { action, habit, dayIndex ->
            viewModel.toggleAction(action, habit, dayIndex)
        }

        setContent {
            HabitTrackerTheme {
                Column {
                    val habits by viewModel.habitsWithActions.observeAsState(emptyList())
                    habits.map {
                        HabitCard(
                            habit = it.habit,
                            actions = it.actions,
                            onActionToggle = onActionToggle
                        )
                    }
                }
            }
        }
    }
}