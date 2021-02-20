package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.AppColor
import com.ofalvai.habittracker.ui.ContentWithPlaceholder
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.Screen
import com.ofalvai.habittracker.ui.dashboard.view.compact.CompactHabitList
import com.ofalvai.habittracker.ui.dashboard.view.fiveday.FiveDayHabitList
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.DashboardConfig
import com.ofalvai.habittracker.ui.model.Habit
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import java.time.LocalDate

private val backgroundBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFFFCC79), AppColor.Light.background),
    start = Offset.Zero,
    end = Offset.Infinite
)

@Composable
fun Dashboard(viewModel: HabitViewModel, navController: NavController) {
    var config by remember { mutableStateOf(DashboardConfig.FiveDay) }
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
        Column(
            Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .statusBarsPadding()
        ) {
            DashboardAppBar(
                config = config,
                onConfigChange = { config = it }
            )

            when (config) {
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
            Icon(painterResource(R.drawable.ic_dashboard_layout), null)
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, stringResource(R.string.common_more))
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
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentWidth()
            .padding(top = 48.dp)
    ) {
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = onAddHabitClick
        ) {
            Icon(Icons.Filled.Add, null, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.dashboard_create_habit_first))
        }
    }
}