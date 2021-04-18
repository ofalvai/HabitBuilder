package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.cancel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun HabitDetailScreen(habitId: Int, navController: NavController) {
    val viewModel: HabitViewModel = viewModel(factory = Dependencies.viewModelFactory)

    val initialState = HabitWithActions(
        Habit(name = "", color = Habit.Color.Blue),
        actions = emptyList(),
        totalActionCount = 0,
        actionHistory = ActionHistory.Clean
    ) // TODO: default color
    val habitWithActions by Transformations.map(viewModel.habitWithActions) { it ?: initialState }
        .observeAsState(initialState)
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
        habitWithActions = habitWithActions,
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
fun HabitDetailScreen(
    habitWithActions: HabitWithActions,
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
        HabitDetailHeader(habitWithActions, onBack, onEdit, onDelete)

        Column(Modifier.padding(32.dp)) {
            CalendarPager(
                yearMonth = yearMonth,
                onPreviousClick = { yearMonth = yearMonth.minusMonths(1) },
                onNextClick = { yearMonth = yearMonth.plusMonths(1) }
            )

            CalendarDayLegend(weekFields = WeekFields.of(Locale.getDefault()))

            HabitCalendar(
                yearMonth = yearMonth,
                habitColor = habitWithActions.habit.color.composeColor,
                actions = habitWithActions.actions,
                onDayToggle = onDayToggle
            )
        }

        HabitStats(habitStats, actionCountByWeek, actionCountByMonth)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HabitDetailHeader(
    habitWithActions: HabitWithActions,
    onBack: () -> Unit,
    onSave: (Habit) -> Unit,
    onDelete: (Habit) -> Unit
) {
    val habitColor = habitWithActions.habit.color.composeColor
    val surfaceColor = habitColor.copy(alpha = 0.5f)
    var isEditing by remember { mutableStateOf(false) }
    var editingName by remember(habitWithActions.habit.name) {
        mutableStateOf(habitWithActions.habit.name)
    }
    var editingColor by remember(habitWithActions.habit.color) {
        mutableStateOf(habitWithActions.habit.color)
    }
    var isNameValid by remember { mutableStateOf(true) }

    val onSaveClick = {
        if (isNameValid) {
            isEditing = false
            val newValue = habitWithActions.habit.copy(name = editingName, color = editingColor)
            onSave(newValue)
        }
    }


    Surface(color = if (isEditing) MaterialTheme.colors.surface else surfaceColor) {
        Column(
            Modifier
                .padding(bottom = 32.dp)
                .statusBarsPadding()) {
            HabitDetailAppBar(
                isEditing = isEditing,
                onBack = onBack,
                onEdit = { isEditing = true },
                onSave = onSaveClick,
                onDelete = { onDelete(habitWithActions.habit) }
            )

            AnimatedVisibility(visible = isEditing) {
                Column {
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
                        initialColor = habitWithActions.habit.color,
                        onColorPick = { editingColor = it }
                    )
                }
            }

            AnimatedVisibility(visible = !isEditing) {
                Column {
                    Text(
                        text = habitWithActions.habit.name,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        style = AppTextStyle.habitTitle
                    )
                    Text(
                        text = stringResource(
                            R.string.habitdetail_total_actions,
                            habitWithActions.totalActionCount
                        ),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}

@Composable
fun HabitDetailAppBar(
    isEditing: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
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
                if (isEditing) {
                    IconButton(onClick = onSave) {
                        Icon(Icons.Rounded.Check, stringResource(R.string.common_save))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, stringResource(R.string.common_delete))
                    }
                } else {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, stringResource(R.string.common_edit))
                    }
                }
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@Composable
fun HabitStats(
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
fun PreviewHabitDetailScreen() {
    HabitTrackerTheme {
        HabitDetailScreen(
            habitWithActions = HabitWithActions(
                Habit(0, "Meditation", Habit.Color.Red),
                listOf(Action(0, true, Instant.now())),
                2,
                ActionHistory.Clean
            ),
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