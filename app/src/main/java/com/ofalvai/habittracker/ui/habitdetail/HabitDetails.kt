package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Transformations
import androidx.navigation.NavController
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.common.HabitColorPicker
import com.ofalvai.habittracker.ui.composeColor
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import kotlinx.coroutines.cancel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun HabitDetailScreen(habitId: Int, viewModel: HabitViewModel, navController: NavController) {
    val initialState = HabitWithActions(
        Habit(name = "", color = Habit.Color.Blue),
        actions = emptyList(),
        totalActionCount = 0
    ) // TODO: default color
    val habitWithActions by Transformations.map(viewModel.habitWithActions) { it ?: initialState }
        .observeAsState(initialState)

    DisposableEffect(habitId) {
        val job = viewModel.fetchHabitDetails(habitId)
        onDispose { job.cancel("Cancelled by Composable") }
    }

    val onDayToggle: (LocalDate, Action) -> Unit = { date, action ->
        viewModel.toggleActionFromDetail(habitId, action, date)
    }

    val onColorPick: (Habit.Color) -> Unit = {
        val newHabit = habitWithActions.habit.copy(color = it)
        viewModel.updateHabit(newHabit)
    }

    HabitDetailScreen(
        habitWithActions = habitWithActions,
        onBack = { navController.popBackStack() },
        onColorPick = onColorPick,
        onDayToggle = onDayToggle
    )
}

@Composable
fun HabitDetailScreen(
    habitWithActions: HabitWithActions,
    onBack: () -> Unit,
    onColorPick: (Habit.Color) -> Unit,
    onDayToggle: (LocalDate, Action) -> Unit,
) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }

    Column {
        HabitDetailHeader(habitWithActions, onBack = onBack, onEdit = { })

        Column(Modifier.padding(32.dp)) {
            HabitColorPicker(initialColor = habitWithActions.habit.color, onColorPick = onColorPick)

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
    }
}

@Composable
fun HabitDetailHeader(
    habitWithActions: HabitWithActions,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val habitColor = habitWithActions.habit.color.composeColor
    val surfaceColor = habitColor.copy(alpha = 0.5f)

    Surface(
        color = surfaceColor
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            HabitDetailAppBar(
                onBack = onBack,
                onEdit = onEdit
            )

            Text(
                text = habitWithActions.habit.name,
                modifier = Modifier.padding(horizontal = 32.dp),
                style = MaterialTheme.typography.h3
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

@Composable
fun HabitDetailAppBar(
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, stringResource(R.string.common_back))
            }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, stringResource(R.string.common_edit))
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewHabitDetailScreen() {
    HabitTrackerTheme {
        HabitDetailScreen(
            habitWithActions = HabitWithActions(
                Habit(0, "Meditation", Habit.Color.Red),
                listOf(Action(0, true, Instant.now())),
                2
            ),
            onBack = { },
            onColorPick = { },
            onDayToggle = { _, _ -> }
        )
    }
}