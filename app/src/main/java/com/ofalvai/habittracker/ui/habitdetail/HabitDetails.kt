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

package com.ofalvai.habittracker.ui.habitdetail

import android.os.Vibrator
import androidx.annotation.FloatRange
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.common.*
import com.ofalvai.habittracker.ui.dashboard.view.VIBRATE_PATTERN_TOGGLE
import com.ofalvai.habittracker.ui.dashboard.view.vibrateCompat
import com.ofalvai.habittracker.ui.model.*
import com.ofalvai.habittracker.ui.theme.AppIcons
import com.ofalvai.habittracker.ui.theme.AppTextStyle
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import com.ofalvai.habittracker.ui.theme.composeColor
import kotlinx.coroutines.cancel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.roundToInt

@Composable
fun HabitDetailScreen(habitId: Int, navController: NavController) {
    val viewModel: HabitDetailViewModel = viewModel(factory = Dependencies.viewModelFactory)
    val vibrator = LocalContext.current.getSystemService<Vibrator>()!!

    val habitDetailState by viewModel.habitWithActions.collectAsState()

    viewModel.habitDetailEvent.asEffect {
        when (it) {
            HabitDetailEvent.BackNavigation -> navController.popBackStack()
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
        viewModel.toggleActionFromDetail(habitId, action, date)
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
        onBack = { navController.popBackStack() },
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
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }

    Column {
        HabitDetailHeader(habitDetailState, singleStats, onBack, onEdit, onArchive)

        Column(Modifier.verticalScroll(rememberScrollState()).padding(32.dp)) {
            when (habitDetailState) {
                is Result.Success -> {
                    CalendarPager(
                        yearMonth = yearMonth,
                        onPreviousClick = { yearMonth = yearMonth.minusMonths(1) },
                        onNextClick = { yearMonth = yearMonth.plusMonths(1) }
                    )
                    CalendarDayLegend(weekFields = WeekFields.of(Locale.getDefault()))
                    HabitCalendar(
                        yearMonth = yearMonth,
                        habitColor = habitDetailState.value.habit.color.composeColor,
                        actions = habitDetailState.value.actions,
                        onDayToggle = onDayToggle
                    )
                }
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
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HabitDetailHeader(
    habitDetailState: Result<HabitWithActions>,
    singleStats: SingleStats,
    onBack: () -> Unit,
    onSave: (Habit) -> Unit,
    onArchive: (Habit) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when (habitDetailState) {
            is Result.Success -> {
                if (isEditing) MaterialTheme.colors.surface else {
                    habitDetailState.value.habit.color.composeColor.copy(alpha = 0.5f)
                }
            }
            else -> MaterialTheme.colors.background
        },
        animationSpec = tween(durationMillis = 900)
    )

    Surface(color = backgroundColor) {
        when (habitDetailState) {
            Result.Loading -> HabitDetailLoadingAppBar(onBack)
            is Result.Failure -> {
                ErrorView(
                    label = stringResource(R.string.habitdetails_error),
                    modifier = Modifier.statusBarsPadding()
                )
            }
            is Result.Success -> {
                AnimatedVisibility(
                    visible = isEditing,
                    enter = fadeIn() + expandVertically(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    HabitHeaderEditingContent(
                        habitName = habitDetailState.value.habit.name,
                        habitDetails = habitDetailState.value,
                        onBack = onBack,
                        onSave = {
                            isEditing = false
                            onSave(it)
                        },
                        onArchive = onArchive
                    )
                }

                AnimatedVisibility(visible = !isEditing, enter = fadeIn(), exit = fadeOut()) {
                    HabitHeaderContent(
                        habitDetails = habitDetailState.value,
                        singleStats = singleStats,
                        onBack = onBack,
                        onEdit = { isEditing = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitHeaderEditingContent(
    habitName: String,
    habitDetails: HabitWithActions,
    onBack: () -> Unit,
    onSave: (Habit) -> Unit,
    onArchive: (Habit) -> Unit
) {
    var editingName by remember(habitName) {
        mutableStateOf(habitName)
    }
    var editingColor by remember(habitDetails.habit.color) {
        mutableStateOf(habitDetails.habit.color)
    }
    var isNameValid by remember { mutableStateOf(true) }
    val onSaveClick = {
        if (isNameValid) {
            val newValue = habitDetails.habit.copy(name = editingName, color = editingColor)
            onSave(newValue)
        }
    }

    Column(
        modifier = Modifier
            .padding(bottom = 32.dp)
            .statusBarsPadding()
    ) {
        HabitDetailEditingAppBar(
            onBack = onBack,
            onSave = onSaveClick,
            onArchive = { onArchive(habitDetails.habit) }
        )
        OutlinedTextField(
            value = editingName,
            onValueChange = {
                editingName = it
                isNameValid = it.isNotBlank()
            },
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            isError = !isNameValid
        )
        HabitColorPicker(
            initialColor = habitDetails.habit.color,
            onColorPick = { editingColor = it }
        )
    }
}

@Composable
private fun HabitHeaderContent(
    habitDetails: HabitWithActions,
    singleStats: SingleStats,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        Modifier
            .padding(bottom = 32.dp)
            .statusBarsPadding()
    ) {
        HabitDetailAppBar(
            onBack = onBack,
            onEdit = onEdit,
        )
        Text(
            text = habitDetails.habit.name,
            modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
            style = AppTextStyle.habitTitle,
            textAlign = TextAlign.Center
        )
        SingleStatRow(
            totalCount = singleStats.actionCount,
            weeklyCount = singleStats.weeklyActionCount,
            completionRate = singleStats.completionRate
        )
    }
}

@Composable
private fun HabitDetailAppBar(
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
            }
        },
        actions = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, stringResource(R.string.common_edit))
                }
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@Composable
private fun HabitDetailEditingAppBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onArchive: () -> Unit
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
            }
        },
        actions = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                IconButton(onClick = onSave) {
                    Icon(Icons.Rounded.Check, stringResource(R.string.common_save))
                }
                IconButton(onClick = onArchive) {
                    Icon(AppIcons.Archive, stringResource(R.string.common_archive))
                }
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@Composable
private fun HabitDetailLoadingAppBar(onBack: () -> Unit) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@Composable
private fun HabitStats(
    chartData: ActionCountChart,
    onStatTypeChange: (ActionCountChart.Type) -> Unit
) {
    Card(Modifier.padding(top = 16.dp)) {
        Column {
            Row(Modifier.align(Alignment.End).padding(top = 8.dp, end = 16.dp)) {
                Text(
                    modifier = Modifier.align(CenterVertically),
                    text = stringResource(R.string.habitdetails_actioncount_selector_label),
                    style = MaterialTheme.typography.body2
                )
                Spacer(Modifier.width(8.dp))
                ToggleButton(
                    checked = chartData.type == ActionCountChart.Type.Weekly,
                    onCheckedChange = { onStatTypeChange(chartData.type.invert()) }
                ) {
                    Text(text = stringResource(R.string.habitdetails_actioncount_selector_weekly))
                }
                Spacer(Modifier.width(8.dp))
                ToggleButton(
                    checked = chartData.type == ActionCountChart.Type.Monthly,
                    onCheckedChange = { onStatTypeChange(chartData.type.invert()) }
                ) {
                    Text(text = stringResource(R.string.habitdetails_actioncount_selector_monthly))
                }

            }
            ActionCountChart(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                values = chartData.items
            )
        }
    }
}

@Composable
private fun ToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    if (checked) {
        Button(onClick = { onCheckedChange(!checked) }) { content() }
    } else {
        OutlinedButton(onClick = { onCheckedChange(!checked) }) { content() }
    }
}

@Composable
private fun SingleStatRow(
    totalCount: Int,
    weeklyCount: Int,
    @FloatRange(from = 0.0, to = 1.0) completionRate: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SingleStat(
            value = totalCount.toString(),
            label = stringResource(R.string.habitdetails_singlestat_total),
            modifier = Modifier.weight(0.33f)
        )
        SingleStat(
            value = weeklyCount.toString(),
            label = stringResource(R.string.habitdetails_singlestat_weekly),
            modifier = Modifier.weight(0.33f)
        )
        SingleStat(
            value = (completionRate * 100).roundToInt().toString() + "%",
            label = stringResource(R.string.habitdetails_singlestat_completionrate),
            modifier = Modifier.weight(0.33f)
        )
    }
}

@Composable
private fun SingleStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = value,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = AppTextStyle.singleStatValue
        )
        Text(
            text = label,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PreviewHabitDetailScreen() {
    HabitTrackerTheme {
        HabitDetailScreen(
            habitDetailState = Result.Success(
                HabitWithActions(
                    Habit(0, "Meditation", Habit.Color.Red),
                    listOf(Action(0, true, Instant.now())),
                    2,
                    ActionHistory.Clean
                )
            ),
            singleStats = SingleStats(LocalDate.now(), 2, 1, 0.15f),
            chartData = Result.Success(ActionCountChart(emptyList(), ActionCountChart.Type.Weekly)),
            onChartTypeChange = {},
            onBack = { },
            onEdit = { },
            onArchive = { },
            onDayToggle = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PreviewSingleStats() {
    HabitTrackerTheme {
        SingleStatRow(totalCount = 18, weeklyCount = 2, completionRate = 0.423555f)
    }
}