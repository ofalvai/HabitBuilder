package com.ofalvai.habittracker.ui.dashboard

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.ofalvai.habittracker.ui.ContentWithPlaceholder
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.Screen
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun Dashboard(viewModel: HabitViewModel, navController: NavController) {
    val habits by viewModel.habitsWithActions.observeAsState(emptyList())

    val onActionToggle: (Action, Habit, Int) -> Unit = { action, habit, dayIndex ->
        val date = LocalDate.now().minus((4 - dayIndex).toLong(), ChronoUnit.DAYS)
        viewModel.toggleAction(habit.id, action, date)
    }
    val onHabitDetail: (Habit) -> (Unit) = {
        navController.navigate(Screen.HabitDetails.buildRoute(it.id))
    }
    val onAddHabitClick = { navController.navigate(Screen.AddHabit.route) }

    ContentWithPlaceholder(
        showPlaceholder = habits.isEmpty(),
        placeholder = { DashboardPlaceholder(onAddHabitClick) }
    ) {
        HabitList(habits, onActionToggle, onHabitDetail, onAddHabitClick)
    }
}

@Composable
fun DashboardPlaceholder(onAddHabitClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().wrapContentWidth().padding(top = 48.dp)) {
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = onAddHabitClick
        ) {
            Icon(Icons.Filled.Add, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Create your first habit")
        }
    }
}

@Composable
fun HabitList(
    habits: List<HabitWithActions>,
    onActionToggle: (Action, Habit, Int) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onAddHabitClick: () -> Unit
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
                    onActionToggle = onActionToggle,
                    onDetailClick = onHabitClick
                )
            }
            item {
                CreateHabitButton(onClick = onAddHabitClick)
            }
        }
    }
}

@Composable
fun CreateHabitButton(
    onClick: () -> Unit
) {
    Box(Modifier.fillMaxWidth().wrapContentWidth()) {
        OutlinedButton(
            modifier = Modifier.padding(16.dp),
            onClick = onClick
        ) {
            Icon(Icons.Filled.Add, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Create new habit")
        }
    }
}