/*
 * Copyright 2022 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ofalvai.habittracker.ui.dashboard.view.fiveday

import android.os.Vibrator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.getSystemService
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.dashboard.view.satisfyingToggleable
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.ActionHistory
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.theme.AppTextStyle
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import com.ofalvai.habittracker.ui.theme.composeColor
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HabitCard(
    habit: Habit,
    actions: List<Action>,
    totalActionCount: Int,
    actionHistory: ActionHistory,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onDetailClick: (Habit) -> Unit,
    dragOffset: Float,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onDetailClick(habit) },
        elevation = if (dragOffset == 0f) 2.dp else 8.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .draggableCard(dragOffset),
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                text = habit.name,
                style = AppTextStyle.habitSubtitle
            )

            ActionHistoryLabel(totalActionCount, actionHistory)

            ActionCircles(
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                actions = actions.takeLast(Constants.DayCount),
                habitColor = habit.color,
                onActionToggle = { action, dayIndex ->
                    val date = LocalDate.now()
                        .minus((Constants.DayCount - 1 - dayIndex).toLong(), ChronoUnit.DAYS)
                    onActionToggle(action, habit, date)
                }
            )
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

    Column(modifier) {
        Row {
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
                    elevation = Dp(index * Constants.ElevationMultiplier),
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
    elevation: Dp,
    onSinglePress: () -> Unit
) {
    val backgroundColor = if (toggled) activeColor else MaterialTheme.colors.surface
    val borderColor = if (toggled) Color.Black.copy(alpha = 0.25f) else activeColor
    val vibrator = LocalContext.current.getSystemService<Vibrator>()!!
    val rippleRadius = remember { Constants.CircleSize / 1.7f } // Make it a bit bigger than D / 2
    val shape = CircleShape

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(Constants.CircleSize)
            .padding(Constants.CirclePadding)
            .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape, false) else Modifier)
            .satisfyingToggleable(
                vibrator, rippleRadius, false, toggled, onToggle, onSinglePress
            )
            .border(BorderStroke(2.dp, borderColor), shape)
            .background(backgroundColor, shape)
            .clip(shape)
    ) {
        if (isHighlighted) {
            Box(
                modifier = Modifier
                    .requiredSize(8.dp)
                    .clip(CircleShape)
                    .background(color = borderColor, shape = CircleShape),
            )
        }
    }
}

@Composable
fun ActionHistoryLabel(totalActionCount: Int, actionHistory: ActionHistory) {
    val resources = LocalContext.current.resources
    val totalLabel = resources.getQuantityString(
        R.plurals.common_action_count_total,
        totalActionCount, totalActionCount
    )
    val actionHistoryLabel = when (actionHistory) {
        ActionHistory.Clean -> stringResource(R.string.common_action_count_clean)
        is ActionHistory.MissedDays -> resources.getQuantityString(
            R.plurals.common_action_count_missed_days, actionHistory.days, actionHistory.days
        )
        is ActionHistory.Streak -> resources.getQuantityString(
            R.plurals.common_action_count_streak, actionHistory.days, actionHistory.days
        )
    }
    val mergedLabel = stringResource(R.string.common_interpunct, totalLabel, actionHistoryLabel)

    Text(
        text = mergedLabel,
        style = MaterialTheme.typography.caption
    )
}

private fun Modifier.draggableCard(
    offset: Float
): Modifier = this
    .zIndex(if (offset == 0f) 0f else 1f)
    .graphicsLayer {
        translationY = if (offset == 0f) 0f else offset
        rotationZ = if (offset == 0f) 0f else -2f
    }


@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewHabitCard() {
    val habit1 = Habit(
        id = 1,
        name = "Meditation",
        color = Habit.Color.Yellow,
        notes = ""
    )
    val habit2 = Habit(
        id = 2,
        name = "Workout",
        color = Habit.Color.Green,
        notes = ""
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
            HabitCard(habit1, actions1, 14, ActionHistory.Clean, { _, _, _ -> }, {}, 0f)
            Spacer(modifier = Modifier.height(16.dp))
            HabitCard(habit2, actions2, 3, ActionHistory.Streak(3), { _, _, _ -> }, {}, 0f)
        }
    }
}