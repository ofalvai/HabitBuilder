package com.ofalvai.habittracker.ui.dashboard.view.fiveday

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun FiveDayHabitList(
    habits: List<HabitWithActions>,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onAddHabitClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp).statusBarsPadding(),
        horizontalAlignment = Alignment.End
    ) {
        val width = Constants.SIZE_CIRCLE * 5 + Constants.PADDING_CIRCLE * 8
        DayLegend(
            modifier = Modifier.wrapContentWidth(Alignment.End).preferredWidth(width).padding(horizontal = 16.dp),
            mostRecentDay = LocalDate.now(),
            pastDayCount = 4
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            items(habits.size) { index ->
                HabitCard(
                    habit = habits[index].habit,
                    actions = habits[index].actions,
                    totalActionCount = habits[index].totalActionCount,
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