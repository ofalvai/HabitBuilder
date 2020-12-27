package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.ContentWithPlaceholder
import com.ofalvai.habittracker.ui.composable.DayLabels
import com.ofalvai.habittracker.ui.composable.HabitCard
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import java.time.LocalDate

@Composable
fun Dashboard(viewModel: DashboardViewModel) {
    val habits by viewModel.habitsWithActions.observeAsState(emptyList())

    val onActionToggle: (Action, Habit, Int) -> Unit = { action, habit, dayIndex ->
        viewModel.toggleAction(action, habit, dayIndex)
    }

    ContentWithPlaceholder(
        showPlaceholder = habits.isEmpty(),
        placeholder = { DashboardPlaceholder() }
    ) {
        HabitList(habits, onActionToggle)
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