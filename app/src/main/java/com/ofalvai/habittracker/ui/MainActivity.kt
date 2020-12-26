package com.ofalvai.habittracker.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.ui.composable.DayLabels
import com.ofalvai.habittracker.ui.composable.HabitCard
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<DashboardViewModel> {
        Dependencies.dashboardViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onActionToggle: (Action, Habit, Int) -> Unit = { action, habit, dayIndex ->
            viewModel.toggleAction(action, habit, dayIndex)
        }

        setContent {
            HabitTrackerTheme {
                Scaffold {
                    val habits by viewModel.habitsWithActions.observeAsState(emptyList())

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        DayLabels(mostRecentDay = LocalDate.now())

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(habits) {
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
    }
}