package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.common.HabitColorPicker
import com.ofalvai.habittracker.ui.habitBlue
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import java.time.YearMonth

@Composable
fun HabitDetailScreen(habitId: Int, viewModel: HabitViewModel) {
    val actions: List<Action> by viewModel.actionsForHabit.observeAsState(emptyList())
    var habitColor by remember(habitId) { mutableStateOf(Habit.Color.Blue) } // TODO: from viewModel
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }

    onCommit(habitId) {
        viewModel.fetchActions(habitId)
    }

    Column(Modifier.padding(32.dp)) {
        HabitColorPicker(color = habitColor, onColorPick = { habitColor = it })

        ActionLog(actions = actions)

        CalendarPager(
            yearMonth = yearMonth,
            onPreviousClick = { yearMonth = yearMonth.minusMonths(1) },
            onNextClick = { yearMonth = yearMonth.plusMonths(1) }
        )
        HabitCalendar(
            yearMonth = yearMonth,
            habitColor = habitColor.composeColor,
            actions = actions,
        )
    }
}

@Composable
fun ActionLog(actions: List<Action>) {
    LazyColumn {
        items(actions) {
            Text(it.timestamp.toString())
        }
    }
}
