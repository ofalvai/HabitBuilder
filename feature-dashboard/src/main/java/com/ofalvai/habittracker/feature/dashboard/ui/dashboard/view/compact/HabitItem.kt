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

package com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.compact

import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitId
import com.ofalvai.habittracker.core.ui.component.HorizontalGrid
import com.ofalvai.habittracker.core.ui.semantics.habitActionSemantics
import com.ofalvai.habittracker.core.ui.theme.AppTextStyle
import com.ofalvai.habittracker.core.ui.theme.CoreIcons
import com.ofalvai.habittracker.core.ui.theme.LocalAppColors
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.core.ui.theme.composeColor
import com.ofalvai.habittracker.feature.dashboard.R
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.satisfyingToggleable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant

@Composable
fun HabitItem(
    habit: Habit,
    actions: ImmutableList<Action>,
    onActionToggle: (Action, Habit, Int) -> Unit,
    onDetailClick: (HabitId) -> Unit,
    dragOffset: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .draggableCard(dragOffset)
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = { onDetailClick(habit.id) })
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically),
                text = habit.name,
                style = AppTextStyle.habitTitleSmall
            )
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = { onDetailClick(habit.id) }
            ) {
                Icon(
                    CoreIcons.ChevronRight,
                    contentDescription = stringResource(R.string.dashboard_item_details)
                )
            }
        }

        ActionSquares(
            actions = actions,
            habitColor = habit.color,
            onActionToggle = { action, dayIndex ->
                val daysInPast = (Constants.DAY_COUNT - 1 - dayIndex)
                onActionToggle(action, habit, daysInPast)
            })
    }
}

@Composable
fun ActionSquares(
    actions: ImmutableList<Action>,
    habitColor: Habit.Color,
    onActionToggle: (Action, Int) -> Unit
) {
    var singlePressCounter by remember { mutableIntStateOf(0) }

    Column {
        HorizontalGrid(Modifier.fillMaxWidth()) {
            actions.mapIndexed { index, action ->
                ActionSquare(
                    activeColor = habitColor.composeColor,
                    action = action,
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
        if (singlePressCounter >= 2) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp),
                text = stringResource(R.string.dashboard_toggle_help),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActionSquare(
    activeColor: Color,
    action: Action,
    onToggle: (Boolean) -> Unit,
    onSinglePress: () -> Unit
) {
    val color = if (action.toggled) activeColor else LocalAppColors.current.gray1
    val borderColor = if (action.toggled) activeColor.darken() else Color.Transparent
    val vibrator = LocalContext.current.getSystemService<Vibrator>()!!

    Box(
        modifier = Modifier
            .habitActionSemantics(action)
            .satisfyingToggleable(vibrator, Dp.Unspecified, true, action.toggled, onToggle, onSinglePress)
            .aspectRatio(1f)
            .padding(1.dp)
            .border(1.dp, borderColor)
            .background(color)
    )
}

private fun Color.darken() = this.copy(
    red = this.red * 0.85f,
    green = this.green * 0.85f,
    blue = this.blue * 0.85f
)

private fun Modifier.draggableCard(
    offset: Float
): Modifier = this
    .zIndex(if (offset == 0f) 0f else 1f)
    .graphicsLayer {
        translationY = if (offset == 0f) 0f else offset
    }

@Preview
@ShowkaseComposable(name = "Compact layout", group = "Dashboard")
@Composable
fun PreviewHabitItem() {
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

    val actions1 = (1..7).map {
        Action(
            id = it,
            toggled = it % 3 == 0,
            timestamp = Instant.now()
        )
    }
    val actions2 = (1..7).map {
        Action(
            id = it,
            toggled = it % 2 == 0,
            timestamp = Instant.now()
        )
    }

    PreviewTheme {
        Column(Modifier.padding(16.dp)) {
            HabitItem(habit1, actions1.toImmutableList(), { _, _, _ -> }, {}, 0f)
            Spacer(modifier = Modifier.height(16.dp))
            HabitItem(habit2, actions2.toImmutableList(), { _, _, _ -> }, {}, 0f)
        }
    }
}