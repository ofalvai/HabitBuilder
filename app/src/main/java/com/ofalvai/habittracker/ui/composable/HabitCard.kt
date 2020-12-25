package com.ofalvai.habittracker.ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.Card
import androidx.compose.material.Shapes
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.shapes
import java.time.Instant
import kotlin.random.Random

@Composable
fun HabitCard(habit: Habit, actions: List<Action>) {
    Card(
        modifier = Modifier.padding(16.dp)
    ) {
        Column {
            Text(text = habit.name)
            ActionCircles(actions)
        }
    }
}

@Composable
fun ActionCircles(actions: List<Action>) {
    Row {
        actions.map {
            ActionCircle(
                modifier = Modifier.size(32.dp).padding(4.dp),
                toggled = it.toggled
            )
        }
    }
}

@Composable
fun ActionCircle(modifier: Modifier = Modifier, toggled: Boolean) {
    if (toggled) {
        Surface(
            shape = CircleShape,
            modifier = modifier,
            color = Color.Red,
        ) { }
    } else {
        Surface(
            shape = CircleShape,
            modifier = modifier,
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
            timestamp = Instant.now(),
            toggled = Random.Default.nextBoolean()
        )
    }
    val actions2 = actions1.shuffled()


    HabitTrackerTheme {
        Column {
            HabitCard(habit1, actions1)
            HabitCard(habit2, actions2)
        }
    }
}