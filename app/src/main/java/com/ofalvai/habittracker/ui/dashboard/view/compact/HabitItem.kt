package com.ofalvai.habittracker.ui.dashboard.view.compact

import android.os.Vibrator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.ofalvai.habittracker.ui.dashboard.view.fiveday.satisfyingToggleable
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HabitItem(
    habit: Habit,
    actions: List<Action>,
    totalActionCount: Int,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onDetailClick: (Habit) -> Unit
) {

    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = { onDetailClick(habit) })
    ) {
        Text(
            text = habit.name,
            style = MaterialTheme.typography.h5
        )

        ActionSquares(
            actions = actions,
            habitColor = habit.color,
            onActionToggle = { action, dayIndex ->
                val date = LocalDate.now().minus((6 - dayIndex).toLong(), ChronoUnit.DAYS) // TODO
                onActionToggle(action, habit, date)
            })
    }
}

@Composable
fun ActionSquares(
    actions: List<Action>,
    habitColor: Habit.Color,
    onActionToggle: (Action, Int) -> Unit
) {
    var singlePressCounter by remember { mutableStateOf(0) }

    Column {
        HorizontalGrid(Modifier.fillMaxWidth()) {
            actions.mapIndexed { index, action ->
                ActionSquare(
                    activeColor = habitColor.composeColor,
                    toggled = action.toggled,
                    onToggle = { newState ->
                        singlePressCounter = 0
                        onActionToggle(
                            action.copy(toggled = newState),
                            index
                        )
                    },
                    onSinglePress = { singlePressCounter++ }
                )
            }
        }
        // TODO
        if (singlePressCounter >= 3) {
            Text(
                modifier = Modifier.align(Alignment.End),
                text = "Long press to toggle",
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HorizontalGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(content, modifier) { measurables, constraints ->
        val itemConstraints = Constraints.fixedWidth(constraints.maxWidth / measurables.size)
        val placeables = measurables.map {
            it.measure(itemConstraints)
        }

        val height = placeables.maxOf { it.height }
        layout(constraints.maxWidth, height) {
            var xOffset = 0

            placeables.forEach {
                it.placeRelative(x = xOffset, y = 0)
                xOffset += it.width
            }
        }
    }
}

@Composable
fun ActionSquare(
    activeColor: Color,
    toggled: Boolean,
    onToggle: (Boolean) -> Unit,
    onSinglePress: () -> Unit
) {
    val color = if (toggled) activeColor else Color.Transparent // TODO
    val secondaryColor = if (toggled) Color.Black.copy(alpha = 0.25f) else activeColor
    val vibrator = AmbientContext.current.getSystemService<Vibrator>()!!

    Surface(
        shape = RectangleShape,
        modifier = Modifier
            .satisfyingToggleable(vibrator, toggled, onToggle, onSinglePress)
            .aspectRatio(1f)
            .padding(1.dp),
        color = color,
        border = BorderStroke(2.dp, secondaryColor)
    ) { }
}