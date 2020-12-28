package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
import com.ofalvai.habittracker.ui.model.Habit

@Composable
fun AddHabitScreen(viewModel: DashboardViewModel, navController: NavController) {
    val onSave: (Habit) -> Unit = {
        viewModel.addHabit(it)
        navController.popBackStack()
    }

    AddHabitForm(onSave)
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
            modifier = Modifier.padding(top = 16.dp),
            onClick = onSaveClick
        ) {
            Text("Save")
        }
    }

}

@Composable
@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
fun PreviewAddHabit() {
    HabitTrackerTheme {
        AddHabitForm(
            onSave = { }
        )
    }
}