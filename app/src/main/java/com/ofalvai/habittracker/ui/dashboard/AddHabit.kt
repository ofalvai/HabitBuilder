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

package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.ui.component.HabitColorPicker
import com.ofalvai.habittracker.core.ui.state.asEffect
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.core.ui.theme.surfaceVariant
import com.ofalvai.habittracker.ui.TextFieldError
import com.ofalvai.habittracker.core.ui.R as coreR

@Composable
fun AddHabitScreen(navController: NavController) {
    val viewModel: AddHabitViewModel = viewModel(factory = Dependencies.viewModelFactory)

    viewModel.backNavigationEvent.asEffect { navController.popBackStack() }

    val onSave: (Habit) -> Unit = { viewModel.addHabit(it) }

    Column(Modifier.statusBarsPadding().verticalScroll(rememberScrollState())) {
        AddHabitAppBar(onBack = { navController.popBackStack() })
        AddHabitForm(onSave)
    }
}

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
            )
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
        )

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.addhabit_notes_description),
            style = MaterialTheme.typography.caption
        )

        HabitColorPicker(color = color, onColorPick = { color = it })

        Button(
            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
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
        items(habits) {
            SuggestionChip(habit = it, onClick = { onSelect(it) })
        }
    }
}

@Composable
private fun AddHabitAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.addhabit_appbar_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, stringResource(coreR.string.common_back))
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
        border = BorderStroke(
            1.dp,
            // Match the OutlinedTextField border color1
            MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
        ),
        color = MaterialTheme.colors.surfaceVariant
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            text = habit,
            style = MaterialTheme.typography.body2
        )
    }
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