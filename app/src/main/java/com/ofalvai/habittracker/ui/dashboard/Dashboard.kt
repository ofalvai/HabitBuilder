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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.core.ui.component.AppBar
import com.ofalvai.habittracker.core.ui.component.ErrorView
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.core.ui.state.asEffect
import com.ofalvai.habittracker.core.ui.theme.AppTextStyle
import com.ofalvai.habittracker.ui.AppIcons
import com.ofalvai.habittracker.ui.ContentWithPlaceholder
import com.ofalvai.habittracker.ui.Destination
import com.ofalvai.habittracker.ui.dashboard.view.compact.CompactHabitList
import com.ofalvai.habittracker.ui.dashboard.view.fiveday.FiveDayHabitList
import com.ofalvai.habittracker.ui.model.DashboardConfig
import com.ofalvai.habittracker.ui.model.OnboardingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController, scaffoldState: ScaffoldState) {
    val viewModel: DashboardViewModel = viewModel(factory = Dependencies.viewModelFactory)

    var config by remember { mutableStateOf(viewModel.dashboardConfig) }
    var configDialogOpen by remember { mutableStateOf(false) }
    val habits by viewModel.habitsWithActions.collectAsState(Result.Loading)
    val onboardingState by viewModel.onboardingState.collectAsState()

    val snackbarCoroutineScope = rememberCoroutineScope()
    DisplaySnackbarEvents(viewModel.dashboardEvent, snackbarCoroutineScope, scaffoldState)

    val onActionToggle: (Action, Habit, Int) -> Unit = { action, habit, daysInPast ->
        viewModel.toggleAction(habit.id, action, daysInPast)
    }
    val onHabitDetail: (Habit) -> (Unit) = {
        navController.navigate(Destination.HabitDetails.buildRoute(it.id))
    }
    val onAddHabitClick = { navController.navigate(Destination.AddHabit.route) }
    val onConfigClick: () -> Unit = { configDialogOpen = true }
    val onConfigChange: (DashboardConfig) -> Unit = {
        config = it
        viewModel.dashboardConfig = it
    }
    val onSettingsClick = { navController.navigate(Destination.Settings.route) }
    val onArchiveClick = { navController.navigate(Destination.Archive.route) }
    val onExportClick = { navController.navigate(Destination.Export.route) }
    val onMove: (ItemMoveEvent) -> Unit = { viewModel.persistItemMove(it) }

    DashboardConfigDialog(
        isVisible = configDialogOpen,
        config = config,
        onConfigSelected = onConfigChange,
        onDismissed = { configDialogOpen = false }
    )

    when (habits) {
        is Result.Success -> {
            LoadedDashboard(
                habits = (habits as Result.Success<List<HabitWithActions>>).value,
                config,
                onboardingState,
                onAddHabitClick,
                onConfigClick,
                onActionToggle,
                onHabitDetail,
                onSettingsClick,
                onArchiveClick,
                onExportClick, // TODO: unify click event listeners
                onMove
            )
        }
        is Result.Failure -> ErrorView(
            label = stringResource(R.string.dashboard_error),
            modifier = Modifier.statusBarsPadding()
        )
        Result.Loading -> {}
    }
}

@Composable
private fun DisplaySnackbarEvents(
    dashboardEvent: Flow<DashboardEvent>,
    snackbarCoroutineScope: CoroutineScope,
    scaffoldState: ScaffoldState
) {
    val errorToggleAction = stringResource(R.string.dashboard_error_toggle_action)
    val errorItemMove = stringResource(R.string.dashboard_error_item_move)
    val eventAction = stringResource(R.string.dashboard_event_action_performed)
    dashboardEvent.asEffect {
        val errorMessage = when (it) {
            DashboardEvent.ToggleActionError -> errorToggleAction
            DashboardEvent.MoveHabitError -> errorItemMove
            DashboardEvent.ActionPerformed -> eventAction
        }
        snackbarCoroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(message = errorMessage)
        }
    }
}

@Composable
private fun LoadedDashboard(
    habits: List<HabitWithActions>,
    config: DashboardConfig,
    onboardingState: OnboardingState?,
    onAddHabitClick: () -> Unit,
    onConfigClick: () -> Unit,
    onActionToggle: (Action, Habit, Int) -> Unit,
    onHabitDetail: (Habit) -> (Unit),
    onSettingsClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onExportClick: () -> Unit,
    onMove: (ItemMoveEvent) -> Unit
) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        DashboardAppBar(onConfigClick, onSettingsClick, onArchiveClick, onExportClick)

        if (onboardingState != null) {
            Onboarding(onboardingState)
        }

        ContentWithPlaceholder(
            showPlaceholder = habits.isEmpty(),
            placeholder = { DashboardPlaceholder(onAddHabitClick) }
        ) {
            Crossfade(targetState = config) {
                when (it) {
                    DashboardConfig.FiveDay -> {
                        FiveDayHabitList(
                            habits, onActionToggle, onHabitDetail, onAddHabitClick, onMove
                        )
                    }
                    DashboardConfig.Compact -> {
                        CompactHabitList(
                            habits, onActionToggle, onHabitDetail, onAddHabitClick, onMove
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardAppBar(
    onConfigClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onExportClick: () -> Unit
) {
    AppBar(
        title = {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = AppTextStyle.screenTitle
            )
        },
        iconActions = {
            IconButton(onClick = onConfigClick) {
                Icon(AppIcons.DashboardLayout, stringResource(R.string.dashboard_change_layout))
            }
        }
    ) {
        DropdownMenuItem(onClick = onArchiveClick) {
            Text(stringResource(R.string.menu_archive))
        }
        DropdownMenuItem(onClick = onSettingsClick) {
            Text(stringResource(R.string.menu_settings))
        }
        DropdownMenuItem(onClick = onExportClick) {
            Text(stringResource(R.string.menu_export))
        }
    }
}

@Composable
private fun DashboardPlaceholder(onAddHabitClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .wrapContentWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.padding(top = 32.dp))

        Image(
            painter = painterResource(R.drawable.illustration_empty_state),
            contentDescription = null
        )

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
