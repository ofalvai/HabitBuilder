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

package com.ofalvai.habittracker.feature.dashboard.ui.addhabit

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.ui.component.AppDefaultAppBar
import com.ofalvai.habittracker.core.ui.component.HabitColorPicker
import com.ofalvai.habittracker.core.ui.component.TextFieldError
import com.ofalvai.habittracker.core.ui.state.asEffect
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.core.ui.theme.composeColor
import com.ofalvai.habittracker.feature.dashboard.R
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.Suggestions
import kotlinx.collections.immutable.ImmutableList
import com.ofalvai.habittracker.core.ui.R as coreR

@Composable
fun AddHabitScreen(viewModel: AddHabitViewModel, navigateBack: () -> Unit) {
    viewModel.backNavigationEvent.asEffect { navigateBack() }

    val onSave: (Habit) -> Unit = { viewModel.addHabit(it) }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        AddHabitAppBar(onBack = navigateBack)
        AddHabitForm(onSave)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitForm(
    onSave: (Habit) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf(Habit.DEFAULT_COLOR) }
    var isNameValid by remember { mutableStateOf(true) }

    val onSaveClick: () -> Unit = {
        if (name.isEmpty()) {
            isNameValid = false
        } else {
            val habit = Habit(0, name, color, notes)
            onSave(habit)
        }
    }
    Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        val nameFocusRequester = remember { FocusRequester() }
        DisposableEffect(Unit) {
            // Using a DisposableEffect with Unit as key to only run the effect once, not in every
            // recomposition
            nameFocusRequester.requestFocus()
            onDispose {  }
        }

        val selectedHabitColor by animateColorAsState(targetValue = color.composeColor, label = "AddHabitForm")
        val customTextFieldColors = OutlinedTextFieldDefaults.colors(
            cursorColor = selectedHabitColor,
            focusedBorderColor = selectedHabitColor,
            focusedLabelColor = selectedHabitColor,
        )
        OutlinedTextField(
            modifier = Modifier
                .focusRequester(nameFocusRequester)
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.addhabit_name_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            colors = customTextFieldColors
        )

        if (!isNameValid) {
            TextFieldError(
                modifier = Modifier.padding(horizontal = 32.dp),
                textError = stringResource(R.string.addhabit_name_error)
            )
        }

        Suggestions(habits = Suggestions.habits, onSelect = {
            name = it
            nameFocusRequester.requestFocus()
        })

        OutlinedTextField(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp).fillMaxWidth(),
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.addhabit_notes_label)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.None
            ),
            colors = customTextFieldColors
        )

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.addhabit_notes_description),
            style = MaterialTheme.typography.bodySmall
        )

        HabitColorPicker(color = color, onColorPick = { color = it })

        Button(
            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
            onClick = onSaveClick,
            colors = ButtonDefaults.buttonColors(containerColor = selectedHabitColor)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                text = stringResource(R.string.addhabit_save)
            )
        }
    }
}

@Composable
private fun Suggestions(habits: ImmutableList<String>, onSelect: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(habits) {
            SuggestionChip(
                onClick = { onSelect(it) },
                label = { Text(it) }
            )
        }
    }
}

@Composable
private fun AddHabitAppBar(onBack: () -> Unit) {
    AppDefaultAppBar(
        title = { Text(stringResource(R.string.addhabit_appbar_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, stringResource(coreR.string.common_back))
            }
        }
    )
}

@Preview
@ShowkaseComposable(name = "Form", group = "Add habit")
@Composable
fun PreviewAddHabit() {
    PreviewTheme {
        Column {
            AddHabitForm(onSave = { })
        }
    }
}

@Preview
@ShowkaseComposable(name = "Suggestions", group = "Add habit")
@Composable
fun PreviewSuggestions() {
    PreviewTheme {
        Suggestions(habits = Suggestions.habits, onSelect = {})
    }
}