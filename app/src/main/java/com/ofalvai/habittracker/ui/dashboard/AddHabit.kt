package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.TextFieldError
import com.ofalvai.habittracker.ui.common.HabitColorPicker
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun AddHabitScreen(viewModel: HabitViewModel, navController: NavController) {
    val onSave: (Habit) -> Unit = {
        viewModel.addHabit(it)
        navController.popBackStack()
    }

    Column(Modifier.statusBarsPadding()) {
        AddHabitAppBar(onBack = { navController.popBackStack() })
        AddHabitForm(onSave)
        Suggestions(habits = Suggestions.habits, onSelect = onSave)
    }
}

@Composable
fun AddHabitForm(
    onSave: (Habit) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf(Habit.DEFAULT_COLOR) }
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
        OutlinedTextField(
            modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.addhabit_name_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        if (!isNameValid) {
            TextFieldError(
                modifier = Modifier.padding(horizontal = 32.dp),
                textError = stringResource(R.string.addhabit_name_error)
            )
        }

        HabitColorPicker(initialColor = color, onColorPick = { color = it })

        Button(
            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
            onClick = onSaveClick
        ) {
            Text(stringResource(R.string.addhabit_save))
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
            text = stringResource(R.string.addhabit_suggestions_title),
            style = MaterialTheme.typography.subtitle1
        )

        LazyRow(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            contentPadding = PaddingValues(start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(habits.size) { index ->
                SuggestionChip(habit = habits[index], onClick = { onSelect(habits[index]) })
            }
        }
    }
}

@Composable
fun AddHabitAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.addhabit_appbar_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@Composable
fun SuggestionChip(habit: Habit, onClick: () -> Unit) {
    val shape = RoundedCornerShape(percent = 50)
    Surface(
        modifier = Modifier.clip(shape).clickable(onClick = onClick),
        shape = shape,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            text = habit.name,
            style = MaterialTheme.typography.body2
        )
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