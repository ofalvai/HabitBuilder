package com.ofalvai.habittracker.ui.dashboard.view.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.dashboard.view.CreateHabitButton
import com.ofalvai.habittracker.ui.dashboard.view.DayLegend
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import java.time.LocalDate

@Composable
fun CompactHabitList(
    habits: List<HabitWithActions>,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onAddHabitClick: () -> Unit
) {
    Column {
        DayLegend(
            modifier = Modifier.fillMaxWidth(),
            mostRecentDay = LocalDate.now(),
            pastDayCount = 6
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            items(habits.size) { index ->
                HabitItem(
                    habit = habits[index].habit,
                    actions = habits[index].actions.takeLast(Constants.DAY_COUNT),
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