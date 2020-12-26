package com.ofalvai.habittracker.ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import kotlin.random.Random

@Composable
fun HabitCard(
    habit: Habit,
    actions: List<Action>,
    onActionToggle: (Action, Habit, Int) -> Unit
) {
    Card(
        modifier = Modifier.padding(16.dp)
    ) {
        Column {
            Text(text = habit.name)
            ActionCircles(actions, onActionToggle = { action, dayIndex ->
                onActionToggle(action, habit, dayIndex)
            })
        }
    }
}

@Composable
fun ActionCircles(actions: List<Action>, onActionToggle: (Action, Int) -> Unit) {
    Row {
        actions.mapIndexed { index, action ->
            ActionCircle(
                modifier = Modifier.size(32.dp).padding(4.dp),
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
            color = Color.Red
        ) { }
    } else {
        Surface(
            shape = CircleShape,
            modifier = modifier.clickable(onClick = { onToggle(true) }),
            border = BorderStroke(2.dp, Color.Red)
        ) { }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHabitCard() {
    val habit1 = Habit(
        id = 1,
        name = "Meditation"
    )
    val habit2 = Habit(
        id = 2,
        name = "Workout"
    )

    val actions1 = (1..5).map {
        Action(
            id = it,
            toggled = Random.Default.nextBoolean()
        )
    }
    val actions2 = actions1.shuffled()


    HabitTrackerTheme {
        Column {
            HabitCard(habit1, actions1, { action, habit, dayIndex -> })
            HabitCard(habit2, actions2, { action, habit, dayIndex -> })
        }
    }
}