/*
 * Copyright 2021 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import com.ofalvai.habittracker.ui.common.ErrorView
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.common.asEffect
import com.ofalvai.habittracker.ui.dashboard.view.compact.CompactHabitList
import com.ofalvai.habittracker.ui.dashboard.view.fiveday.FiveDayHabitList
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.DashboardConfig
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import com.ofalvai.habittracker.ui.theme.AppIcons
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun Dashboard(navController: NavController, scaffoldState: ScaffoldState) {
    val viewModel: DashboardViewModel = viewModel(factory = Dependencies.viewModelFactory)

    var config by remember { mutableStateOf(viewModel.dashboardConfig) }
    val habits by viewModel.habitsWithActions.collectAsState(Result.Loading)

    val snackbarCoroutineScope = rememberCoroutineScope()
    val errorMessage = stringResource(R.string.dashboard_error_toggle_action)
    viewModel.dashboardEvent.asEffect {
        when (it) {
            DashboardEvent.ToggleActionError -> {
                snackbarCoroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = errorMessage)
                }
            }
        }
    }

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
    val onAboutClick = { navController.navigate(Screen.About.route) }

    when (habits) {
        is Result.Success -> {
            LoadedDashboard(
                habits = (habits as Result.Success<List<HabitWithActions>>).value,
                config = config,
                onAddHabitClick,
                onConfigChange,
                onActionToggle,
                onHabitDetail,
                onAboutClick
            )
        }
        is Result.Failure -> ErrorView(
            label = stringResource(R.string.dashboard_error),
            modifier = Modifier.statusBarsPadding()
        )
        Result.Loading -> { }
    }
}

@Composable
private fun LoadedDashboard(
    habits: List<HabitWithActions>,
    config: DashboardConfig,
    onAddHabitClick: () -> Unit,
    onConfigChange: (DashboardConfig) -> Unit,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onHabitDetail: (Habit) -> (Unit),
    onAboutClick: () -> Unit
) {
    ContentWithPlaceholder(
        showPlaceholder = habits.isEmpty(),
        placeholder = { DashboardPlaceholder(onAddHabitClick, onAboutClick) }
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
        ) {
            DashboardAppBar(config, onConfigChange, onAboutClick)

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
private fun DashboardAppBar(
    config: DashboardConfig,
    onConfigChange: (DashboardConfig) -> Unit,
    onAboutClick: () -> Unit
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
                DropdownMenuItem(onClick = onAboutClick) {
                    Text(stringResource(R.string.menu_about))
                }
            }
        }
    }
}

@Composable
private fun DashboardPlaceholder(
    onAddHabitClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DashboardAppBar(
            config = DashboardConfig.FiveDay,
            onConfigChange = {},
            onAboutClick = onAboutClick
        )

        Spacer(Modifier.padding(top = 32.dp))

        Image(painter = painterResource(R.drawable.illustration_empty_state), contentDescription = "")

        Text(
            text = stringResource(R.string.dashboard_empty_label),
            style = MaterialTheme.typography.body1
        )

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