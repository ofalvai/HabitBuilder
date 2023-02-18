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

package com.ofalvai.habittracker.feature.dashboard.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.core.common.OnboardingState
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitId
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.core.ui.component.AppBarOverflowMenuAction
import com.ofalvai.habittracker.core.ui.component.AppDefaultRootAppBar
import com.ofalvai.habittracker.core.ui.component.ContentWithPlaceholder
import com.ofalvai.habittracker.core.ui.component.ErrorView
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.core.ui.state.asEffect
import com.ofalvai.habittracker.core.ui.theme.AppTextStyle
import com.ofalvai.habittracker.core.ui.theme.CoreIcons
import com.ofalvai.habittracker.feature.dashboard.R
import com.ofalvai.habittracker.feature.dashboard.ui.DashboardIcons
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.compact.CompactHabitList
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.fiveday.FiveDayHabitList
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.minicalendar.MiniCalendarHabitList
import com.ofalvai.habittracker.feature.dashboard.ui.model.DashboardConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.ofalvai.habittracker.core.ui.R as coreR

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    snackbarHostState: SnackbarHostState,
    navigateToDetails: (HabitId) -> Unit,
    navigateToAddHabit: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToArchive: () -> Unit,
    navigateToExport: () -> Unit
) {
    var config by remember { mutableStateOf(viewModel.dashboardConfig) }
    var configDialogOpen by remember { mutableStateOf(false) }
    val habits by viewModel.habitsWithActions.collectAsState(Result.Loading)
    val onboardingState by viewModel.onboardingState.collectAsState()

    val snackbarCoroutineScope = rememberCoroutineScope()
    DisplaySnackbarEvents(viewModel.dashboardEvent, snackbarCoroutineScope, snackbarHostState)

    val onActionToggle: (Action, Habit, Int) -> Unit = { action, habit, daysInPast ->
        viewModel.toggleAction(habit.id, action, daysInPast)
    }
    val onConfigClick: () -> Unit = { configDialogOpen = true }
    val onConfigChange: (DashboardConfig) -> Unit = {
        config = it
        viewModel.dashboardConfig = it
    }
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
                habits = (habits as Result.Success<ImmutableList<HabitWithActions>>).value,
                config,
                onboardingState,
                navigateToAddHabit,
                onConfigClick,
                onActionToggle,
                navigateToDetails,
                navigateToSettings,
                navigateToArchive,
                navigateToExport, // TODO: unify click event listeners
                onMove
            )
        }
        is Result.Failure -> ErrorView(label = stringResource(R.string.dashboard_error))
        Result.Loading -> {}
    }
}

@Composable
private fun DisplaySnackbarEvents(
    dashboardEvent: Flow<DashboardEvent>,
    snackbarCoroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
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
            snackbarHostState.showSnackbar(message = errorMessage)
        }
    }
}

@Composable
private fun LoadedDashboard(
    habits: ImmutableList<HabitWithActions>,
    config: DashboardConfig,
    onboardingState: OnboardingState?,
    onAddHabitClick: () -> Unit,
    onConfigClick: () -> Unit,
    onActionToggle: (Action, Habit, Int) -> Unit,
    onHabitDetail: (HabitId) -> (Unit),
    onSettingsClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onExportClick: () -> Unit,
    onMove: (ItemMoveEvent) -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                    DashboardConfig.MiniCalendar -> {
                        MiniCalendarHabitList(
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
    AppDefaultRootAppBar(
        title = {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = AppTextStyle.screenTitle
            )
        },
        actions = {
            IconButton(onClick = onConfigClick) {
                Icon(DashboardIcons.DashboardLayout, stringResource(R.string.dashboard_change_layout))
            }
            AppBarOverflowMenuAction {
                DropdownMenuItem(
                    onClick = onArchiveClick,
                    text = { Text(stringResource(coreR.string.menu_archive)) },
                    leadingIcon = { Icon(painter = CoreIcons.Archive, contentDescription = null) },
                )
                DropdownMenuItem(
                    onClick = onExportClick,
                    text = { Text(stringResource(coreR.string.menu_export)) },
                    leadingIcon = { Icon(painter = CoreIcons.Export, contentDescription = null) },
                )
                DropdownMenuItem(
                    onClick = onSettingsClick,
                    text = { Text(stringResource(coreR.string.menu_settings)) },
                    leadingIcon = { Icon(painter = CoreIcons.Settings, contentDescription = null) },
                )
            }
        }
    )
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
            style = MaterialTheme.typography.bodyMedium
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
