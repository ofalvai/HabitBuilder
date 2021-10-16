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
import androidx.compose.material.ButtonDefaults.textButtonColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.ofalvai.habittracker.mapper.mapActionCountByMonthListToItemList
import com.ofalvai.habittracker.mapper.mapActionCountByWeekListToItemList
import com.ofalvai.habittracker.ui.common.*
import com.ofalvai.habittracker.ui.dashboard.view.VIBRATE_PATTERN_TOGGLE
import com.ofalvai.habittracker.ui.dashboard.view.vibrateCompat
import com.ofalvai.habittracker.ui.model.*
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
    val actionCountByWeek by viewModel.actionCountByWeek.collectAsState()
    val actionCountByMonth by viewModel.actionCountByMonth.collectAsState()

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

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingHabitToDelete by remember { mutableStateOf<Habit?>(null) }
    val onDelete: (Habit) -> Unit = {
        showDeleteDialog = true
        pendingHabitToDelete = it
    }

    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            pendingHabitToDelete?.let { viewModel.deleteHabit(it) }
            pendingHabitToDelete = null
        }
    )

    HabitDetailScreen(
        habitDetailState = habitDetailState,
        singleStats = singleStats,
        actionCountByWeek = actionCountByWeek,
        actionCountByMonth = actionCountByMonth,
        onBack = { navController.popBackStack() },
        onEdit = { viewModel.updateHabit(it) },
        onDelete = onDelete,
        onDayToggle = onDayToggle
    )
}

@Composable
private fun HabitDetailScreen(
    habitDetailState: Result<HabitWithActions>,
    singleStats: SingleStats,
    actionCountByWeek: List<ActionCountByWeek>,
    actionCountByMonth: List<ActionCountByMonth>,
    onBack: () -> Unit,
    onEdit: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    onDayToggle: (LocalDate, Action) -> Unit,
) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }

    Column {
        HabitDetailHeader(habitDetailState, singleStats, onBack, onEdit, onDelete)

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

                    HabitStats(actionCountByWeek, actionCountByMonth)
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
    onDelete: (Habit) -> Unit
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
                        onDelete = onDelete
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
    onDelete: (Habit) -> Unit
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
            onDelete = { onDelete(habitDetails.habit) }
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
    onDelete: () -> Unit
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
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, stringResource(R.string.common_delete))
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

private enum class HabitStatType {
    Weekly, Monthly;

    fun invert() = if (this == Weekly) Monthly else Weekly
}

@Composable
private fun HabitStats(
    actionCountByWeek: List<ActionCountByWeek>,
    actionCountByMonth: List<ActionCountByMonth>
) {
    var habitStatType by remember { mutableStateOf(HabitStatType.Weekly) }
    val chartItems by remember(habitStatType, actionCountByMonth, actionCountByWeek) {
        val chartItems: List<ChartItem> = when (habitStatType) {
            HabitStatType.Weekly -> mapActionCountByWeekListToItemList(actionCountByWeek)
            HabitStatType.Monthly -> mapActionCountByMonthListToItemList(actionCountByMonth)
        }
        mutableStateOf(chartItems)
    }

    Column {
        Row(Modifier.align(Alignment.End)) {
            ToggleButton(
                checked = habitStatType == HabitStatType.Weekly,
                onCheckedChange = { habitStatType = habitStatType.invert() }
            ) {
                Text(text = stringResource(R.string.habitdetails_actioncount_selector_weekly))
            }
            Spacer(Modifier.width(8.dp))
            ToggleButton(
                checked = habitStatType == HabitStatType.Monthly,
                onCheckedChange = { habitStatType = habitStatType.invert() }
            ) {
                Text(text = stringResource(R.string.habitdetails_actioncount_selector_monthly))
            }

        }
        ActionCountChart(
            modifier = Modifier.fillMaxWidth(),
            values = chartItems
        )
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

@Composable
private fun DeleteConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = textButtonColors(contentColor = MaterialTheme.colors.error)
                ) {
                    Text(text = stringResource(R.string.habitdetails_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(text = stringResource(R.string.habitdetails_confirm_cancel))
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.habitdetails_confirm_title),
                    style = MaterialTheme.typography.h6
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.habitdetails_delete_description),
                    style = MaterialTheme.typography.body1
                )
            }
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
            actionCountByWeek = emptyList(),
            actionCountByMonth = emptyList(),
            onBack = { },
            onEdit = { },
            onDelete = { },
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