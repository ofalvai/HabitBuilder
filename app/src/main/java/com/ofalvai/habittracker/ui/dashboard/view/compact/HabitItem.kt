package com.ofalvai.habittracker.ui.dashboard.view.compact

import android.os.Vibrator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.ofalvai.habittracker.ui.dashboard.view.satisfyingToggleable
import com.ofalvai.habittracker.ui.inactiveDay
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HabitItem(
    habit: Habit,
    actions: List<Action>,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onDetailClick: (Habit) -> Unit
) {

    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = { onDetailClick(habit) })
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterVertically),
                text = habit.name,
                style = MaterialTheme.typography.subtitle1
            )
            IconButton(
                onClick = { onDetailClick(habit) }
            ) {
                Icon(Icons.Filled.KeyboardArrowRight)
            }
        }

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
        if (singlePressCounter >= 3) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 16.dp),
                text = "Long press to toggle",
                style = MaterialTheme.typography.body1,
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
    val color = if (toggled) activeColor else inactiveDay
    val vibrator = AmbientContext.current.getSystemService<Vibrator>()!!

    Surface(
        shape = RectangleShape,
        modifier = Modifier
            .satisfyingToggleable(vibrator, Dp.Unspecified, true, toggled, onToggle, onSinglePress)
            .aspectRatio(1f)
            .padding(1.dp),
        color = color,
    ) { }
}