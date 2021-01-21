package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Transformations
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.common.HabitColorPicker
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun HabitDetailScreen(habitId: Int, viewModel: HabitViewModel) {
    val initialState = HabitWithActions(Habit(name = "", color = Habit.Color.Blue), actions = emptyList()) // TODO: default color
    val habitWithActions by Transformations.map(viewModel.habitWithActions) { it ?: initialState }
        .observeAsState(initialState)

    var yearMonth by remember { mutableStateOf(YearMonth.now()) }

    onCommit(habitId) {
        viewModel.fetchHabitDetails(habitId)
    }

    val onDayToggle: (LocalDate, Action) -> Unit = { date, action ->
        viewModel.toggleAction(habitId, action, date)
        viewModel.fetchHabitDetails(habitId)
    }

    Column(Modifier.padding(32.dp)) {
        HabitColorPicker(initialColor = habitWithActions.habit.color, onColorPick = {
            val newHabit = habitWithActions.habit.copy(color = it)
            viewModel.updateHabit(newHabit)
        })

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
