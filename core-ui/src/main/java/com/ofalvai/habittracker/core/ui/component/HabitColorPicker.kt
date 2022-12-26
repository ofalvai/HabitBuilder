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

package com.ofalvai.habittracker.core.ui.component

import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.ui.theme.LocalAppColors
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.core.ui.theme.composeColor

enum class ColorPickerState { Default, Selected }

@Composable
fun HabitColorPicker(
    color: Habit.Color,
    onColorPick: (Habit.Color) -> Unit
) {
    val colors = remember { Habit.Color.values().toList() }

    LazyRow(
        Modifier.padding(vertical = 32.dp).fillMaxWidth(),
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(colors) {
            val isSelected = it == color
            val transition: Transition<ColorPickerState> = updateTransition(
                targetState = if (isSelected) ColorPickerState.Selected else ColorPickerState.Default,
                label = "ColorPickerTransition"
            )

            HabitColor(
                color = it,
                transition = transition,
                onClick = { onColorPick(it) }
            )
        }
    }
}

@Composable
fun HabitColor(
    color: Habit.Color,
    transition: Transition<ColorPickerState>,
    onClick: () -> Unit
) {
    val animationSpec = tween<Dp>(durationMillis = 200)
    val size: Dp by transition.animateDp(
        transitionSpec = { animationSpec }, label = "ColorPickerSize"
    ) { state ->
        when (state) {
            ColorPickerState.Default -> 48.dp
            ColorPickerState.Selected -> 64.dp
        }
    }
    val horizontalPadding: Dp by transition.animateDp(
        transitionSpec = { animationSpec }, label = "ColorPickerHorizontalPadding"
    ) { state ->
        when (state) {
            ColorPickerState.Default -> 8.dp
            ColorPickerState.Selected -> 0.dp
        }
    }
    val verticalPadding: Dp by transition.animateDp(
        transitionSpec = { animationSpec }, label = "ColorPickerVerticalPadding"
    ) { state ->
        when (state) {
            ColorPickerState.Default -> 8.dp
            ColorPickerState.Selected -> 0.dp
        }
    }
    val checkmarkSize: Dp by transition.animateDp(
        transitionSpec = { animationSpec }, label = "ColorPickerCheckmarkSize"
    ) { state ->
        when (state) {
            ColorPickerState.Default -> 0.dp
            ColorPickerState.Selected -> 24.dp
        }
    }

    Box(
        modifier = Modifier
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .size(size)
            .clickable(
                onClick = onClick,
                onClickLabel = color.name,
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .border(BorderStroke(1.dp, LocalAppColors.current.gray2), CircleShape)
            .background(color.composeColor, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            tint = MaterialTheme.colorScheme.surface,
            contentDescription = null,
            modifier = Modifier.requiredSize(checkmarkSize)
        )
    }
}

@Preview
@ShowkaseComposable(name = "Color picker", "Common")
@Composable
fun PreviewHabitColorPicker() {
    PreviewTheme {
        var color by remember { mutableStateOf(Habit.Color.Yellow) }
        HabitColorPicker(color = color, onColorPick = { color = it })
    }
}