package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.HabitViewModel
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

    val onSaveClick: () -> Unit = {
        val randomColor = Habit.Color.values().random()
        val habit = Habit(
            name = name,
            color = randomColor
        )
        onSave(habit)
    }

    Column(Modifier.padding(32.dp).fillMaxWidth()) {
        // TODO: keyboard IME actions, focus
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Habit name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Button(
            modifier = Modifier.padding(top = 8.dp),
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
@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
fun PreviewAddHabit() {
    HabitTrackerTheme {
        Column {
            Suggestions(habits = Suggestions.habits, onSelect = { })

            AddHabitForm(onSave = { })
        }
    }
}