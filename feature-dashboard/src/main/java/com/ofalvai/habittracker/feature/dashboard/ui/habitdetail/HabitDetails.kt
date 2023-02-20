/*
 * Copyright 2022 Olivér Falvai
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

package com.ofalvai.habittracker.feature.dashboard.ui.habitdetail

import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.common.VIBRATE_PATTERN_TOGGLE
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.ActionHistory
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.core.ui.component.CalendarDayLegend
import com.ofalvai.habittracker.core.ui.component.CalendarPager
import com.ofalvai.habittracker.core.ui.component.ConfirmationDialog
import com.ofalvai.habittracker.core.ui.component.ErrorView
import com.ofalvai.habittracker.core.ui.recomposition.StableHolder
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.core.ui.state.asEffect
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.core.ui.theme.composeColor
import com.ofalvai.habittracker.feature.dashboard.R
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.vibrateCompat
import com.ofalvai.habittracker.feature.dashboard.ui.model.ActionCountChart
import com.ofalvai.habittracker.feature.dashboard.ui.model.SingleStats
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.cancel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HabitDetailScreen(
    viewModel: HabitDetailViewModel,
    habitId: Int,
    navigateBack: () -> Unit
) {
    val vibrator = LocalContext.current.getSystemService<Vibrator>()!!

    val habitDetailState by viewModel.habitWithActions.collectAsState()

    viewModel.habitDetailEvent.asEffect {
        when (it) {
            HabitDetailEvent.BackNavigation -> navigateBack()
        }
    }

    val singleStats by viewModel.singleStats.collectAsState()
    val chartData by viewModel.chartData.collectAsState()

    DisposableEffect(habitId) {
        val job = viewModel.fetchHabitDetails(habitId)
        onDispose { job.cancel("Cancelled by Composable") }
    }
    DisposableEffect(habitId) {
        val job = viewModel.fetchHabitStats(habitId)
        onDispose { job.cancel("Cancelled by Composable") }
    }

    val onDayToggle: (LocalDate, Action) -> Unit = { date, action ->
        vibrator.vibrateCompat(VIBRATE_PATTERN_TOGGLE)
        viewModel.toggleAction(habitId, action, date)
    }

    var showArchiveDialog by remember { mutableStateOf(false) }
    var pendingHabitToArchive by remember { mutableStateOf<Habit?>(null) }
    val onArchive: (Habit) -> Unit = {
        showArchiveDialog = true
        pendingHabitToArchive = it
    }

    ConfirmationDialog(
        showDialog = showArchiveDialog,
        title = stringResource(R.string.habitdetails_archive_title),
        description = stringResource(R.string.habitdetails_archive_description),
        confirmText = stringResource(R.string.habitdetails_archive_confirm),
        onDismiss = { showArchiveDialog = false },
        onConfirm = {
            pendingHabitToArchive?.let { viewModel.archiveHabit(it) }
            pendingHabitToArchive = null
            showArchiveDialog = false
        }
    )

    HabitDetailScreen(
        habitDetailState = habitDetailState,
        singleStats = singleStats,
        chartData = chartData,
        onChartTypeChange = { viewModel.switchChartType(it) },
        onBack = navigateBack,
        onEdit = { viewModel.updateHabit(it) },
        onArchive = onArchive,
        onDayToggle = onDayToggle
    )
}

@Composable
private fun HabitDetailScreen(
    habitDetailState: Result<HabitWithActions>,
    singleStats: SingleStats,
    chartData: Result<ActionCountChart>,
    onChartTypeChange: (ActionCountChart.Type) -> Unit,
    onBack: () -> Unit,
    onEdit: (Habit) -> Unit,
    onArchive: (Habit) -> Unit,
    onDayToggle: (LocalDate, Action) -> Unit,
) {
    Column(Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
        val scrollState = rememberScrollState()

        HabitDetailHeader(habitDetailState, singleStats, scrollState, onBack, onEdit, onArchive)

        Column(Modifier.verticalScroll(scrollState).padding(16.dp)) {
            when (habitDetailState) {
                is Result.Success -> Calendar(habitDetailState, onDayToggle)
                Result.Loading -> {
                    // No calendar and stats in loading state
                }
                is Result.Failure -> {
                    ErrorView(
                        label = stringResource(R.string.habitdetails_error_stats),
                        modifier = Modifier.padding(top = 64.dp)
                    )
                }
            }

            when (chartData) {
                is Result.Success -> HabitStats(chartData.value, onChartTypeChange)
                is Result.Failure -> ErrorView(
                    label = stringResource(R.string.habitdetails_error_stats),
                    modifier = Modifier.padding(top = 16.dp)
                )
                Result.Loading -> {
                    // No chart in loading state
                }
            }

            // Add some scrollable empty space. This helps the collapsible header avoid oscillating
            // between the collapsed and full height states
            Spacer(Modifier.height(150.dp))
        }
    }
}

@Composable
private fun Calendar(
    habitDetailState: Result.Success<HabitWithActions>,
    onDayToggle: (LocalDate, Action) -> Unit
) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    Card {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)
        ) {
            CalendarPager(
                yearMonth = StableHolder(yearMonth),
                onPreviousClick = { yearMonth = yearMonth.minusMonths(1) },
                onNextClick = { yearMonth = yearMonth.plusMonths(1) }
            )
            CalendarDayLegend()
            HabitCalendar(
                yearMonth = StableHolder(yearMonth),
                habitColor = habitDetailState.value.habit.color.composeColor,
                actions = habitDetailState.value.actions,
                onDayToggle = onDayToggle,
                onMonthSwipe = { yearMonth = it }
            )
        }
    }
}

@Composable
private fun HabitStats(
    chartData: ActionCountChart,
    onStatTypeChange: (ActionCountChart.Type) -> Unit
) {
    Card(Modifier.padding(top = 16.dp)) {
        Row(Modifier.padding(top = 8.dp, start = 16.dp)) {
            Text(
                modifier = Modifier.align(CenterVertically),
                text = stringResource(R.string.habitdetails_actioncount_selector_label),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.width(8.dp))
            ToggleButton(
                checked = chartData.type == ActionCountChart.Type.Weekly,
                onCheckedChange = { onStatTypeChange(chartData.type.invert()) },
                shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
            ) {
                Text(text = stringResource(R.string.habitdetails_actioncount_selector_weekly))
            }
            ToggleButton(
                checked = chartData.type == ActionCountChart.Type.Monthly,
                onCheckedChange = { onStatTypeChange(chartData.type.invert()) },
                shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
            ) {
                Text(text = stringResource(R.string.habitdetails_actioncount_selector_monthly))
            }
        }
        ActionCountChart(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            values = chartData.items
        )
    }
}


@Composable
private fun ToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape = RoundedCornerShape(percent = 50),
    content: @Composable () -> Unit
) {
    // Replace with SegmentedButton once available in Compose Material3
    OutlinedButton(
        onClick = {
            if (!checked) { onCheckedChange(false) }
        },
        colors = toggleButtonColors(checked),
        shape = shape
    ) { content() }
}

@Composable
private fun toggleButtonColors(checked: Boolean): ButtonColors {
    return if (checked) {
        ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    } else {
        ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@ShowkaseComposable(name = "Screen", group = "Habit details")
@Composable
fun PreviewHabitDetailScreen() {
    PreviewTheme {
        HabitDetailScreen(
            habitDetailState = Result.Success(
                HabitWithActions(
                    Habit(0, "Meditation", Habit.Color.Red, ""),
                    persistentListOf(Action(0, true, Instant.now())),
                    2,
                    ActionHistory.Clean
                )
            ),
            singleStats = SingleStats(LocalDate.now(), 2, 1, 0.15f),
            chartData = Result.Success(ActionCountChart(
                persistentListOf(
                    ActionCountChart.ChartItem("W23", 2022, 3),
                    ActionCountChart.ChartItem("W24", 2022, 0),
                    ActionCountChart.ChartItem("W25", 2022, 6),
                ),
                ActionCountChart.Type.Weekly)
            ),
            onChartTypeChange = {},
            onBack = { },
            onEdit = { },
            onArchive = { },
            onDayToggle = { _, _ -> }
        )
    }
}