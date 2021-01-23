package com.ofalvai.habittracker.ui.dashboard.view.fiveday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.dashboard.view.CreateHabitButton
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import java.time.LocalDate

@Composable
fun FiveDayHabitList(
    habits: List<HabitWithActions>,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onAddHabitClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        DayLabels(mostRecentDay = LocalDate.now())

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            items(habits) {
                HabitCard(
                    habit = it.habit,
                    actions = it.actions,
                    totalActionCount = it.totalActionCount,
                    onActionToggle = onActionToggle,
                    onDetailClick = onHabitClick
                )
            }
            item {
                CreateHabitButton(onClick = onAddHabitClick)
            }
        }
    }
}