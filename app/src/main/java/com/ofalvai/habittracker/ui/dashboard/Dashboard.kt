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
import com.ofalvai.habittracker.ui.ContentWithPlaceholder
import com.ofalvai.habittracker.ui.Destination
import com.ofalvai.habittracker.ui.common.AppBar
import com.ofalvai.habittracker.ui.common.ErrorView
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.common.asEffect
import com.ofalvai.habittracker.ui.dashboard.view.compact.CompactHabitList
import com.ofalvai.habittracker.ui.dashboard.view.fiveday.FiveDayHabitList
import com.ofalvai.habittracker.ui.model.*
import com.ofalvai.habittracker.ui.theme.AppIcons
import com.ofalvai.habittracker.ui.theme.AppTextStyle
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController, scaffoldState: ScaffoldState) {
    val viewModel: DashboardViewModel = viewModel(factory = Dependencies.viewModelFactory)

    var config by remember { mutableStateOf(viewModel.dashboardConfig) }
    var configDialogOpen by remember { mutableStateOf(false) }
    val habits by viewModel.habitsWithActions.collectAsState(Result.Loading)
    val onboardingState by viewModel.onboardingState.collectAsState()

    val snackbarCoroutineScope = rememberCoroutineScope()
    val errorToggleAction = stringResource(R.string.dashboard_error_toggle_action)
    val errorItemMove = stringResource(R.string.dashboard_error_item_move)
    viewModel.dashboardEvent.asEffect {
        val errorMessage = when (it) {
            DashboardEvent.ToggleActionError -> errorToggleAction
            DashboardEvent.MoveHabitError -> errorItemMove
        }
        snackbarCoroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(message = errorMessage)
        }
    }

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
    val onAboutClick = { navController.navigate(Destination.About.route) }
    val onArchiveClick = { navController.navigate(Destination.Archive.route) }
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
                onAboutClick,
                onArchiveClick,
                onMove
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
    onboardingState: OnboardingState?,
    onAddHabitClick: () -> Unit,
    onConfigClick: () -> Unit,
    onActionToggle: (Action, Habit, Int) -> Unit,
    onHabitDetail: (Habit) -> (Unit),
    onAboutClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onMove: (ItemMoveEvent) -> Unit
) {
    ContentWithPlaceholder(
        showPlaceholder = habits.isEmpty(),
        placeholder = { DashboardPlaceholder(onAddHabitClick, onAboutClick, onArchiveClick, onConfigClick) }
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
        ) {
            DashboardAppBar(onConfigClick, onAboutClick, onArchiveClick)

            if (onboardingState != null) {
                Onboarding(onboardingState)
            }

            Crossfade(targetState = config) {
                when (it) {
                    DashboardConfig.FiveDay -> {
                        FiveDayHabitList(habits, onActionToggle, onHabitDetail, onAddHabitClick, onMove)
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
    onConfigClick: () -> Unit,
    onAboutClick: () -> Unit,
    onArchiveClick: () -> Unit
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
        },
        dropdownMenuItems = {
            DropdownMenuItem(onClick = onArchiveClick) {
                Text(stringResource(R.string.menu_archive))
            }
            DropdownMenuItem(onClick = onAboutClick) {
                Text(stringResource(R.string.menu_about))
            }
        }
    )
}

@Composable
private fun DashboardPlaceholder(
    onAddHabitClick: () -> Unit,
    onAboutClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onConfigClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DashboardAppBar(onConfigClick, onAboutClick, onArchiveClick)

        Spacer(Modifier.padding(top = 32.dp))

        Image(
            painter = painterResource(R.drawable.illustration_empty_state),
            contentDescription = ""
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
