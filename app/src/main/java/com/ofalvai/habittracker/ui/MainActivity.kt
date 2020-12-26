package com.ofalvai.habittracker.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.ui.composable.DayLabels
import com.ofalvai.habittracker.ui.composable.HabitCard
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import java.time.LocalDate
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<DashboardViewModel> {
        Dependencies.dashboardViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onActionToggle: (Action, Habit, Int) -> Unit = { action, habit, dayIndex ->
            viewModel.toggleAction(action, habit, dayIndex)
        }

        val onNewHabit: () -> Unit = {
            viewModel.addHabit(
                Habit(
                    name = "Habit ${Random.Default.nextInt(0, 100)}"
                )
            )
        }

        setContent {
            HabitTrackerTheme {
                Scaffold(
                    topBar = { MainTopBar(onNewHabit = onNewHabit) }
                ) {
                    val habits by viewModel.habitsWithActions.observeAsState(emptyList())

                    ContentWithPlaceholder(
                        showPlaceholder = habits.isEmpty(),
                        placeholder = { DashboardPlaceholder() }
                    ) {
                        HabitList(habits, onActionToggle)
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
fun DashboardPlaceholder() {
    Text(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        text = "Create your first habit by tapping + above",
        style = MaterialTheme.typography.h5,
        textAlign = TextAlign.Center
    )
}

@Composable
fun HabitList(
    habits: List<HabitWithActions>,
    onActionToggle: (Action, Habit, Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        DayLabels(mostRecentDay = LocalDate.now())

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
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