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

package com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.minicalendar

import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.getSystemService
import com.ofalvai.habittracker.core.common.VIBRATE_PATTERN_TOGGLE
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitId
import com.ofalvai.habittracker.core.ui.semantics.habitActionSemantics
import com.ofalvai.habittracker.core.ui.theme.AppTextStyle
import com.ofalvai.habittracker.core.ui.theme.composeColor
import com.ofalvai.habittracker.core.ui.theme.composeContainerColor
import com.ofalvai.habittracker.core.ui.theme.composeOnContainerColor
import com.ofalvai.habittracker.feature.dashboard.R
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.vibrateCompat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habit: Habit,
    actions: ImmutableList<Action>,
    onActionToggle: (Action, Habit, Int) -> Unit,
    onDetailClick: (HabitId) -> Unit,
    dragOffset: Float,
    modifier: Modifier = Modifier
) {
    val vibrator = LocalContext.current.getSystemService<Vibrator>()!!
    val isDragging = dragOffset != 0f
    Card(
        onClick = { onDetailClick(habit.id) },
        colors = CardDefaults.cardColors(
            containerColor = habit.color.composeContainerColor,
            contentColor = habit.color.composeOnContainerColor
        ),
        border = if (isDragging) BorderStroke(1.dp, habit.color.composeColor) else null,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .draggableCard(dragOffset),
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                text = habit.name,
                style = AppTextStyle.habitTitle
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val todayAction = actions.last()
                ActionButton(
                    color = habit.color,
                    toggled = todayAction.toggled,
                    onActionToggle = {
                        vibrator.vibrateCompat(VIBRATE_PATTERN_TOGGLE)
                        val newAction = todayAction.copy(toggled = it)
                        onActionToggle(newAction, habit, 0)
                    }
                )

                ActionCalendar(
                    actions = actions.takeLast(30).toImmutableList(),
                    habitColor = habit.color
                )
            }
        }
    }
}

@Composable
fun ActionCalendar(
    actions: ImmutableList<Action>,
    habitColor: Habit.Color
) {
    Column {
        (0 until Constants.Rows).forEach { rowIndex ->
            Row(modifier = Modifier.padding(vertical = Constants.DotPadding)) {
                val row = actions.subList(
                    rowIndex * Constants.ItemPerRow,
                    (rowIndex + 1) * Constants.ItemPerRow
                )
                row.forEach {
                    ActionDot(action = it, habitColor = habitColor)
                }
            }
        }
    }
}

@Composable
fun ActionDot(
    action: Action,
    habitColor: Habit.Color
) {
    val color = if (action.toggled) habitColor.composeColor else MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .habitActionSemantics(action)
            .padding(horizontal = Constants.DotPadding)
            .background(color, shape = RoundedCornerShape(2.dp))
            .size(12.dp)
    )
}

@Composable
fun ActionButton(
    color: Habit.Color,
    toggled: Boolean,
    onActionToggle: (Boolean) -> Unit
) {
    Button(
        modifier = Modifier.animateContentSize().padding(end = 8.dp),
        onClick = { onActionToggle(!toggled) },
        colors = ButtonDefaults.buttonColors(
            containerColor = color.composeColor
        )
    ) {
        AnimatedVisibility(toggled) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        val label = stringResource(if (toggled) R.string.dashboard_toggle_label_toggled else R.string.dashboard_toggle_label_untoggled)
        Text(text = label)
    }
}

private fun Modifier.draggableCard(
    offset: Float
): Modifier = this
    .zIndex(if (offset == 0f) 0f else 1f)
    .graphicsLayer {
        translationY = if (offset == 0f) 0f else offset
        rotationZ = if (offset == 0f) 0f else -2f
    }