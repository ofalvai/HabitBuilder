package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.blue
import com.ofalvai.habittracker.ui.green
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import kotlin.random.Random

private val SIZE_ACTION = 48.dp

@Composable
fun HabitCard(
    habit: Habit,
    actions: List<Action>,
    onActionToggle: (Action, Habit, Int) -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        backgroundColor = habit.color.composeColor // TODO: dark/light mode
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.h5
            )

            ActionCircles(
                modifier = Modifier.align(Alignment.End).padding(top = 16.dp),
                actions = actions,
                onActionToggle = { action, dayIndex ->
                    onActionToggle(action, habit, dayIndex)
                })
        }
    }
}

@Composable
fun ActionCircles(
    modifier: Modifier,
    actions: List<Action>,
    onActionToggle: (Action, Int) -> Unit
) {
    Row(modifier) {
        actions.mapIndexed { index, action ->
            ActionCircle(
                modifier = Modifier.size(SIZE_ACTION).padding(4.dp),
                toggled = action.toggled,
                onToggle = { newState -> onActionToggle(action.copy(toggled = newState), index) }
            )
        }
    }
}

@Composable
fun ActionCircle(modifier: Modifier = Modifier, toggled: Boolean, onToggle: (Boolean) -> Unit) {
    if (toggled) {
        Surface(
            shape = CircleShape,
            modifier = modifier.clickable(onClick = { onToggle(false) }),
            color = green
        ) { }
    } else {
        Surface(
            shape = CircleShape,
            modifier = modifier.clickable(onClick = { onToggle(true) }),
            border = BorderStroke(2.dp, blue)
        ) { }
    }
}

@Composable
fun DayLabels(
    modifier: Modifier = Modifier,
    mostRecentDay: LocalDate,
    pastDayCount: Int = 4
) {
    Row(modifier.padding(horizontal = 16.dp).wrapContentWidth(Alignment.End)) {
        (pastDayCount downTo 0).map {
            DayLabel(day = mostRecentDay.minusDays(it.toLong()))
        }
    }
}

@Composable
fun DayLabel(
    day: LocalDate
) {
    Column(Modifier.size(SIZE_ACTION).wrapContentHeight(Alignment.CenterVertically)) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = day.dayOfMonth.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption
        )
    }
}

@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewDayLabels() {
    HabitTrackerTheme {
        DayLabels(
            modifier = Modifier.padding(horizontal = 16.dp),
            mostRecentDay = LocalDate.now())
    }
}


@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewHabitCard() {
    val habit1 = Habit(
        id = 1,
        name = "Meditation",
        color = Habit.Color.Yellow
    )
    val habit2 = Habit(
        id = 2,
        name = "Workout",
        color = Habit.Color.White
    )

    val actions1 = (1..5).map {
        Action(
            id = it,
            toggled = Random.Default.nextBoolean()
        )
    }
    val actions2 = actions1.shuffled()


    HabitTrackerTheme {
        Column(Modifier.padding(16.dp)) {
            HabitCard(habit1, actions1, { action, habit, dayIndex -> })
            Spacer(modifier = Modifier.height(16.dp))
            HabitCard(habit2, actions2, { action, habit, dayIndex -> })
        }
    }
}