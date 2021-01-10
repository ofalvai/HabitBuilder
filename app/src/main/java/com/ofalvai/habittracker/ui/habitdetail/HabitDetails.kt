package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.common.HabitColorPicker
import com.ofalvai.habittracker.ui.habitBlue
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit

@Composable
fun HabitDetailScreen(habitId: Int, viewModel: HabitViewModel) {
    val actions: List<Action> by viewModel.actionsForHabit.observeAsState(emptyList())
    var habitColor by remember { mutableStateOf(Habit.Color.Blue) } // TODO: from viewModel

    onCommit(habitId) {
        viewModel.fetchActions(habitId)
    }

    Column {
        HabitColorPicker(color = habitColor, onColorPick = { habitColor = it })

        ActionLog(actions = actions)

        HabitCalendar(habitColor = habitColor.composeColor, actions = actions)
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
