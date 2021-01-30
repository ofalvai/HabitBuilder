package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.ContentWithPlaceholder
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.Screen
import com.ofalvai.habittracker.ui.dashboard.view.compact.CompactHabitList
import com.ofalvai.habittracker.ui.dashboard.view.fiveday.FiveDayHabitList
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.DashboardConfig
import com.ofalvai.habittracker.ui.model.Habit
import java.time.LocalDate

val dashboardConfig = DashboardConfig.FiveDay

@Composable
fun Dashboard(viewModel: HabitViewModel, navController: NavController) {
    val habits by viewModel.habitsWithActions.observeAsState(emptyList())

    val onActionToggle: (Action, Habit, LocalDate) -> Unit = { action, habit, date ->
        viewModel.toggleActionFromDashboard(habit.id, action, date)
    }
    val onHabitDetail: (Habit) -> (Unit) = {
        navController.navigate(Screen.HabitDetails.buildRoute(it.id))
    }
    val onAddHabitClick = { navController.navigate(Screen.AddHabit.route) }

    ContentWithPlaceholder(
        showPlaceholder = habits.isEmpty(),
        placeholder = { DashboardPlaceholder(onAddHabitClick) }
    ) {
        when (dashboardConfig) {
            DashboardConfig.FiveDay -> {
                FiveDayHabitList(habits, onActionToggle, onHabitDetail, onAddHabitClick)
            }
            DashboardConfig.Compact -> {
                CompactHabitList(habits, onActionToggle, onHabitDetail, onAddHabitClick)
            }
        }
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
            Text(stringResource(R.string.dashboard_create_habit_first))
        }
    }
}