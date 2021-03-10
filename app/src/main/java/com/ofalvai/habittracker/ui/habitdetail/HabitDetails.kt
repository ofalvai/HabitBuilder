package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.navigation.NavController
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.common.HabitColorPicker
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.ActionHistory
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
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
fun HabitDetailScreen(habitId: Int, viewModel: HabitViewModel, navController: NavController) {
    val initialState = HabitWithActions(
        Habit(name = "", color = Habit.Color.Blue),
        actions = emptyList(),
        totalActionCount = 0,
        actionHistory = ActionHistory.Clean
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

    HabitDetailScreen(
        habitWithActions = habitWithActions,
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
            onBack = { },
            onEdit = { },
            onDelete = { },
            onDayToggle = { _, _ -> }
        )
    }
}