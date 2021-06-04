package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.ContentWithPlaceholder
import com.ofalvai.habittracker.ui.Screen
import com.ofalvai.habittracker.ui.dashboard.view.compact.CompactHabitList
import com.ofalvai.habittracker.ui.dashboard.view.fiveday.FiveDayHabitList
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.DashboardConfig
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.theme.AppIcons
import java.time.LocalDate

@Composable
fun Dashboard(navController: NavController) {
    val viewModel: DashboardViewModel = viewModel(factory = Dependencies.viewModelFactory)

    var config by remember { mutableStateOf(viewModel.dashboardConfig) }
    val habits by viewModel.habitsWithActions.collectAsState(emptyList())

    val onActionToggle: (Action, Habit, LocalDate) -> Unit = { action, habit, date ->
        viewModel.toggleActionFromDashboard(habit.id, action, date)
    }
    val onHabitDetail: (Habit) -> (Unit) = {
        navController.navigate(Screen.HabitDetails.buildRoute(it.id))
    }
    val onAddHabitClick = { navController.navigate(Screen.AddHabit.route) }
    val onConfigChange: (DashboardConfig) -> Unit = {
        config = it
        viewModel.dashboardConfig = it
    }

    ContentWithPlaceholder(
        showPlaceholder = habits.isEmpty(),
        placeholder = { DashboardPlaceholder(onAddHabitClick) }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            DashboardAppBar(
                config = config,
                onConfigChange = onConfigChange
            )

            Crossfade(targetState = config) {
                when (it) {
                    DashboardConfig.FiveDay -> {
                        FiveDayHabitList(habits, onActionToggle, onHabitDetail, onAddHabitClick)
                    }
                    DashboardConfig.Compact -> {
                        CompactHabitList(habits, onActionToggle, onHabitDetail, onAddHabitClick)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardAppBar(
    config: DashboardConfig,
    onConfigChange: (DashboardConfig) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val onConfigChangeClick = {
        when (config) {
            DashboardConfig.FiveDay -> onConfigChange(DashboardConfig.Compact)
            DashboardConfig.Compact -> onConfigChange(DashboardConfig.FiveDay)
        }
    }

    Row(modifier = Modifier.height(48.dp)) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp),
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.h6
        )

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onConfigChangeClick) {
            Icon(AppIcons.DashboardLayout, stringResource(R.string.dashboard_change_layout))
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Rounded.MoreVert, stringResource(R.string.common_more))
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                offset = DpOffset(8.dp, 0.dp)
            ) {
                DropdownMenuItem(onClick = { }) {
                    Text("Settings")
                }
            }
        }
    }
}

@Composable
fun DashboardPlaceholder(onAddHabitClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DashboardAppBar(config = DashboardConfig.FiveDay, onConfigChange = { })

        Spacer(Modifier.padding(top = 32.dp))

        Button(
            modifier = Modifier.padding(16.dp),
            onClick = onAddHabitClick
        ) {
            Icon(Icons.Rounded.Add, null, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.dashboard_create_habit_first))
        }
    }
}