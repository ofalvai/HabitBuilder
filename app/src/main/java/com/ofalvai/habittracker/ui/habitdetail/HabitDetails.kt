package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.common.CalendarDayLegend
import com.ofalvai.habittracker.ui.common.CalendarPager
import com.ofalvai.habittracker.ui.common.HabitColorPicker
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

sealed class HabitDetailState {
    object Loading: HabitDetailState()

    data class Loaded(val habitDetails: HabitWithActions): HabitDetailState()
}

@Composable
fun HabitDetailScreen(habitId: Int, navController: NavController) {
    val viewModel: HabitViewModel = viewModel(factory = Dependencies.viewModelFactory)

    val initialState = HabitDetailState.Loading
    val habitDetailState by Transformations.map(viewModel.habitWithActions) {
        if (it == null) {
            initialState
        } else {
            HabitDetailState.Loaded(it)
        }
    }.observeAsState(initialState)

    val habitStats by viewModel.habitStats.observeAsState(GeneralHabitStats(null, 0, 0f))
    val actionCountByWeek by viewModel.actionCountByWeek.observeAsState(emptyList())
    val actionCountByMonth by viewModel.actionCountByMonth.observeAsState(emptyList())

    DisposableEffect(habitId) {
        val job = viewModel.fetchHabitDetails(habitId)
        onDispose { job.cancel("Cancelled by Composable") }
    }
    DisposableEffect(habitId) {
        val job = viewModel.fetchHabitStats(habitId)
        onDispose { job.cancel("Cancelled by Composable") }
    }

    val onDayToggle: (LocalDate, Action) -> Unit = { date, action ->
        viewModel.toggleActionFromDetail(habitId, action, date)
    }

    HabitDetailScreen(
        habitDetailState = habitDetailState,
        habitStats = habitStats,
        actionCountByWeek = actionCountByWeek,
        actionCountByMonth = actionCountByMonth,
        onBack = { navController.popBackStack() },
        onEdit = { viewModel.updateHabit(it) },
        onDelete = {
            viewModel.deleteHabit(it)
            navController.popBackStack()
        },
        onDayToggle = onDayToggle
    )
}

@Composable
private fun HabitDetailScreen(
    habitDetailState: HabitDetailState,
    habitStats: GeneralHabitStats,
    actionCountByWeek: List<ActionCountByWeek>,
    actionCountByMonth: List<ActionCountByMonth>,
    onBack: () -> Unit,
    onEdit: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    onDayToggle: (LocalDate, Action) -> Unit,
) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }

    Column {
        HabitDetailHeader(habitDetailState, onBack, onEdit, onDelete)

        Column(Modifier.padding(32.dp)) {
            when (habitDetailState) {
                is HabitDetailState.Loaded -> {
                    CalendarPager(
                        yearMonth = yearMonth,
                        onPreviousClick = { yearMonth = yearMonth.minusMonths(1) },
                        onNextClick = { yearMonth = yearMonth.plusMonths(1) }
                    )
                    CalendarDayLegend(weekFields = WeekFields.of(Locale.getDefault()))
                    HabitCalendar(
                        yearMonth = yearMonth,
                        habitColor = habitDetailState.habitDetails.habit.color.composeColor,
                        actions = habitDetailState.habitDetails.actions,
                        onDayToggle = onDayToggle
                    )
                }
                HabitDetailState.Loading -> {
                    // No calendar in loading state
                }
            }
        }

        HabitStats(habitStats, actionCountByWeek, actionCountByMonth)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HabitDetailHeader(
    habitDetailState: HabitDetailState,
    onBack: () -> Unit,
    onSave: (Habit) -> Unit,
    onDelete: (Habit) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when (habitDetailState) {
            is HabitDetailState.Loaded -> {
                if (isEditing) MaterialTheme.colors.surface else {
                    habitDetailState.habitDetails.habit.color.composeColor.copy(alpha = 0.5f)
                }
            }
            HabitDetailState.Loading -> MaterialTheme.colors.background
        },
        animationSpec = tween(durationMillis = 900)
    )

    Surface(color = backgroundColor) {
        when (habitDetailState) {
            HabitDetailState.Loading -> HabitDetailLoadingAppBar(onBack)

            is HabitDetailState.Loaded -> {
                AnimatedVisibility(
                    visible = isEditing,
                    enter = fadeIn() + expandVertically(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    HabitHeaderEditingContent(
                        habitName = habitDetailState.habitDetails.habit.name,
                        habitDetails = habitDetailState.habitDetails,
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
                        habitName = habitDetailState.habitDetails.habit.name,
                        habitDetails = habitDetailState.habitDetails,
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

    Column(modifier = Modifier
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
    habitName: String,
    habitDetails: HabitWithActions,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        Modifier
            .padding(bottom = 32.dp)
            .statusBarsPadding()) {
        HabitDetailAppBar(
            onBack = onBack,
            onEdit = onEdit,
        )
        Text(
            text = habitName,
            modifier = Modifier.padding(horizontal = 32.dp),
            style = AppTextStyle.habitTitle
        )
        Text(
            text = stringResource(
                R.string.habitdetail_total_actions,
                habitDetails.totalActionCount
            ),
            modifier = Modifier.padding(horizontal = 32.dp),
            style = MaterialTheme.typography.body1
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

@Composable
private fun HabitStats(
    generalStats: GeneralHabitStats,
    actionCountByWeek: List<ActionCountByWeek>,
    actionCountByMonth: List<ActionCountByMonth>
) {
    Column(Modifier.padding(horizontal = 32.dp)) {
        Text("First day: ${generalStats.firstDay.toString()}")
        Text("Action count: ${generalStats.actionCount}")
        Text("Completion rate: ${generalStats.completionRate * 100}%")

        Spacer(modifier = Modifier.height(16.dp))
        Text("Actions by week:")
        actionCountByWeek.forEach {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${it.year} W${it.weekOfYear}: ${it.actionCount}"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        actionCountByMonth.forEach {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${it.yearMonth}: ${it.actionCount}"
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PreviewHabitDetailScreen() {
    HabitTrackerTheme {
        HabitDetailScreen(
            habitDetailState = HabitDetailState.Loaded(HabitWithActions(
                Habit(0, "Meditation", Habit.Color.Red),
                listOf(Action(0, true, Instant.now())),
                2,
                ActionHistory.Clean
            )),
            habitStats = GeneralHabitStats(LocalDate.now(), 2, 0.15f),
            actionCountByWeek = emptyList(),
            actionCountByMonth = emptyList(),
            onBack = { },
            onEdit = { },
            onDelete = { },
            onDayToggle = { _, _ -> }
        )
    }
}