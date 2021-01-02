package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.*
import androidx.compose.animation.transition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.TextFieldError
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.Suggestions

@Composable
fun AddHabitScreen(viewModel: HabitViewModel, navController: NavController) {
    val onSave: (Habit) -> Unit = {
        viewModel.addHabit(it)
        navController.popBackStack()
    }

    Column {
        Suggestions(habits = Suggestions.habits, onSelect = onSave)
        AddHabitForm(onSave)
    }
}

@Composable
fun AddHabitForm(
    onSave: (Habit) -> Unit
) {
    var name by savedInstanceState { "" }
    var color by savedInstanceState { Habit.DEFAULT_COLOR }
    var isNameValid by remember { mutableStateOf(true) }

    val onSaveClick: () -> Unit = {
        if (name.isEmpty()) {
            isNameValid = false
        } else {
            val habit = Habit(
                name = name,
                color = color
            )
            onSave(habit)
        }
    }

    Column(Modifier.fillMaxWidth()) {
        // TODO: keyboard IME actions, focus
        TextField(
            modifier = Modifier.padding(horizontal = 32.dp),
            value = name,
            onValueChange = { name = it },
            label = { Text("Habit name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        if (!isNameValid) {
            TextFieldError(
                modifier = Modifier.padding(horizontal = 32.dp),
                textError = "Enter a name for new habit"
            )
        }

        HabitColorPicker(onColorPick = { color = it })

        Button(
            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
            onClick = onSaveClick
        ) {
            Text("Save")
        }
    }
}

@Composable
fun Suggestions(habits: List<Habit>, onSelect: (Habit) -> Unit) {
    Column(
        Modifier.padding(vertical = 32.dp).fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = "Try one of these:",
            style = MaterialTheme.typography.subtitle1
        )

        LazyRow(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            contentPadding = PaddingValues(start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(habits) {
                SuggestionChip(habit = it, onClick = { onSelect(it) })
            }
        }
    }
}

@Composable
fun SuggestionChip(habit: Habit, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            text = habit.name,
            style = MaterialTheme.typography.body2
        )
    }
}

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
    Surface(
        modifier = Modifier
            .padding(horizontal = state[horizontalPaddingKey], vertical = state[verticalPaddingKey])
            .size(size)
            .clickable(onClick = onClick, indication = rememberRipple(radius = size / 2)),
        shape = CircleShape,
        color = color.composeColor,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.15f))
    ) {
        if (isSelected) {
            Icon(Icons.Filled.Check, tint = Color.Black.copy(alpha = 0.75f))
        }
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

@Composable
@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
fun PreviewAddHabit() {
    HabitTrackerTheme {
        Column {
            Suggestions(habits = Suggestions.habits, onSelect = { })

            AddHabitForm(onSave = { })
        }
    }
}