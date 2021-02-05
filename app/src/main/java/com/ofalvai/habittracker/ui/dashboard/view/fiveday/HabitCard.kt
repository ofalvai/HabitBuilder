package com.ofalvai.habittracker.ui.dashboard.view.fiveday

import android.os.Vibrator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.composeColor
import com.ofalvai.habittracker.ui.dashboard.view.satisfyingToggleable
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@Composable
fun HabitCard(
    habit: Habit,
    actions: List<Action>,
    totalActionCount: Int,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onDetailClick: (Habit) -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = { onDetailClick(habit) }),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.h6
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.dashboard_fiveday_total_actions, totalActionCount),
                    style = MaterialTheme.typography.caption
                )

                ActionCircles(
                    modifier = Modifier.padding(top = 16.dp),
                    actions = actions.takeLast(Constants.DAY_COUNT),
                    habitColor = habit.color,
                    onActionToggle = { action, dayIndex ->
                        val date = LocalDate.now()
                            .minus((Constants.DAY_COUNT - 1 - dayIndex).toLong(), ChronoUnit.DAYS)
                        onActionToggle(action, habit, date)
                    })
            }
        }
    }
}

@Composable
fun ActionCircles(
    modifier: Modifier,
    actions: List<Action>,
    habitColor: Habit.Color,
    onActionToggle: (Action, Int) -> Unit
) {
    var singlePressCounter by remember { mutableStateOf(0) }

    Column {
        Row(modifier) {
            actions.mapIndexed { index, action ->
                ActionCircle(
                    activeColor = habitColor.composeColor,
                    toggled = action.toggled,
                    onToggle = { newState ->
                        singlePressCounter = 0
                        onActionToggle(
                            action.copy(toggled = newState),
                            index
                        )
                    },
                    isHighlighted = index == actions.size - 1,
                    onSinglePress = { singlePressCounter++ }
                )
            }
        }
        if (singlePressCounter >= 3) {
            Text(
                modifier = Modifier.align(Alignment.End),
                text = stringResource(R.string.dashboard_toggle_help),
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.Center
            )
        }
    }

}

@Composable
fun ActionCircle(
    activeColor: Color,
    toggled: Boolean,
    onToggle: (Boolean) -> Unit,
    isHighlighted: Boolean,
    onSinglePress: () -> Unit
) {
    val color = if (toggled) activeColor else Color.Transparent
    val secondaryColor = if (toggled) Color.Black.copy(alpha = 0.25f) else activeColor
    val vibrator = AmbientContext.current.getSystemService<Vibrator>()!!
    val rippleRadius = remember { Constants.SIZE_CIRCLE / 1.7f } // Make it a bit bigger than D / 2

    Surface(
        shape = CircleShape,
        modifier = Modifier
            .satisfyingToggleable(vibrator, rippleRadius, false, toggled, onToggle, onSinglePress)
            .size(Constants.SIZE_CIRCLE)
            .padding(Constants.PADDING_CIRCLE),
        color = color,
        border = BorderStroke(2.dp, secondaryColor)
    ) {
        if (isHighlighted) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(8.dp),
                color = secondaryColor
            ) { }
        }
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
        color = Habit.Color.Green
    )

    val actions1 = (1..5).map {
        Action(
            id = it,
            toggled = Random.Default.nextBoolean(),
            timestamp = Instant.now()
        )
    }
    val actions2 = actions1.shuffled()

    HabitTrackerTheme {
        Column(Modifier.padding(16.dp)) {
            HabitCard(habit1, actions1, 14, { _, _, _ -> }, {})
            Spacer(modifier = Modifier.height(16.dp))
            HabitCard(habit2, actions2, 3, { _, _, _ -> }, {})
        }
    }
}