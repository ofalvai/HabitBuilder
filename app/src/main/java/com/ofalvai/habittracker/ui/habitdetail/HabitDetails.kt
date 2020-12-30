package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.onCommit
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.model.Action

@Composable
fun HabitDetailScreen(habitId: Int, viewModel: HabitViewModel) {
    val actions: List<Action> by viewModel.actionsForHabit.observeAsState(emptyList())

    onCommit(habitId) {
        viewModel.fetchActions(habitId)
    }

    ActionLog(actions = actions)
}

@Composable
fun ActionLog(actions: List<Action>) {
    LazyColumn() {
        items(actions) {
            Text(it.timestamp.toString())
        }
    }
}

