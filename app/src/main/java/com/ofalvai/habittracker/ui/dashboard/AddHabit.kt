/*
 * Copyright 2021 Olivér Falvai
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

package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.TextFieldError
import com.ofalvai.habittracker.ui.common.HabitColorPicker
import com.ofalvai.habittracker.ui.common.observeAsEffect
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme

@Composable
fun AddHabitScreen(navController: NavController) {
    val viewModel: AddHabitViewModel = viewModel(factory = Dependencies.viewModelFactory)

    viewModel.backNavigationEvent.observeAsEffect { navController.popBackStack() }

    val onSave: (Habit) -> Unit = {
        viewModel.addHabit(it)
    }

    Column(Modifier.statusBarsPadding().verticalScroll(rememberScrollState())) {
        AddHabitAppBar(onBack = { navController.popBackStack() })
        AddHabitForm(onSave)
    }
}

@Composable
private fun AddHabitForm(
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
        val focusRequester = remember { FocusRequester() }
        SideEffect {
            focusRequester.requestFocus()
        }

        OutlinedTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth(),
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

        Suggestions(habits = Suggestions.habits, onSelect = { name = it })

        HabitColorPicker(initialColor = color, onColorPick = { color = it })

        Button(
            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp).fillMaxWidth(),
            onClick = onSaveClick
        ) {
            Text(stringResource(R.string.addhabit_save))
        }
    }
}

@Composable
private fun Suggestions(habits: List<String>, onSelect: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(habits.size) { index ->
            SuggestionChip(habit = habits[index], onClick = { onSelect(habits[index]) })
        }
    }
}

@Composable
private fun AddHabitAppBar(onBack: () -> Unit) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SuggestionChip(habit: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(percent = 50)
    Surface(
        shape = shape,
        onClick = onClick,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.15f))
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            text = habit,
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