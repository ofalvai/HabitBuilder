package com.ofalvai.habittracker.ui.common

import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TransitionState
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.model.Habit

@Composable
fun HabitColorPicker(
    onColorPick: (Habit.Color) -> Unit
) {
    val colors = remember { Habit.Color.values().toList() }
    var selectionIndex by remember { mutableStateOf(0) }

    LazyRow(
        Modifier.padding(vertical = 32.dp).fillMaxWidth(),
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(colors) {
            val isSelected = selectionIndex == colors.indexOf(it)
            val state = transition(
                definition = colorPickerTransition,
                toState = if (isSelected) ColorPickerState.Selected else ColorPickerState.Default,
            )

            HabitColor(
                color = it,
                isSelected = isSelected,
                state = state,
                onClick = {
                    selectionIndex = colors.indexOf(it)
                    onColorPick(it)
                }
            )
        }
    }
}

@Composable
fun HabitColor(
    color: Habit.Color,
    isSelected: Boolean,
    state: TransitionState,
    onClick: () -> Unit
) {
    val size = state[sizeKey]
    val modifier = Modifier
        .padding(horizontal = state[horizontalPaddingKey], vertical = state[verticalPaddingKey])
        .size(size)
        .clickable(
            onClick = onClick,
            indication = rememberRipple(radius = size / 2, bounded = false)
        )
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.composeColor,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.15f))
    ) {
        if (isSelected) {
            Icon(Icons.Filled.Check, tint = Color.Black.copy(alpha = 0.75f))
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
fun PreviewHabitColorPicker() {
    HabitTrackerTheme {
        HabitColorPicker(onColorPick = { })
    }
}

private enum class ColorPickerState { Default, Selected }

private val sizeKey = DpPropKey("size")
private val horizontalPaddingKey = DpPropKey("horizontal_padding")
private val verticalPaddingKey = DpPropKey("vertical_padding")

private val colorPickerTransition = transitionDefinition<ColorPickerState> {
    state(ColorPickerState.Default) {
        this[sizeKey] = 48.dp
        this[horizontalPaddingKey] = 4.dp
        this[verticalPaddingKey] = 8.dp
    }

    state(ColorPickerState.Selected) {
        this[sizeKey] = 56.dp
        this[horizontalPaddingKey] = 0.dp
        this[verticalPaddingKey] = 0.dp
    }

    val animationSpec = tween<Dp>(easing = LinearOutSlowInEasing, durationMillis = 250)
    transition(fromState = ColorPickerState.Default, toState = ColorPickerState.Selected) {
        sizeKey using animationSpec
        horizontalPaddingKey using animationSpec
        verticalPaddingKey using animationSpec
    }

    transition(fromState = ColorPickerState.Selected, toState = ColorPickerState.Default) {
        sizeKey using animationSpec
        horizontalPaddingKey using animationSpec
        verticalPaddingKey using animationSpec
    }
}