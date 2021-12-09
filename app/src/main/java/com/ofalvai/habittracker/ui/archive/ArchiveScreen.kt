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

package com.ofalvai.habittracker.ui.archive

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.common.ConfirmationDialog
import com.ofalvai.habittracker.ui.common.ErrorView
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.common.asEffect
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import kotlinx.coroutines.launch

@Composable
fun ArchiveScreen(navController: NavController, scaffoldState: ScaffoldState) {
    val viewModel = viewModel<ArchiveViewModel>(factory = Dependencies.viewModelFactory)

    val habits by viewModel.archivedHabitList.collectAsState(initial = Result.Loading)

    val snackbarCoroutineScope = rememberCoroutineScope()
    val errorUnarchive = stringResource(R.string.archive_error_unarchive)
    val errorDelete = stringResource(R.string.archive_error_delete)
    viewModel.archiveEvent.asEffect {
        val message = when (it) {
            ArchiveEvent.UnarchiveError -> errorUnarchive
            ArchiveEvent.DeleteError -> errorDelete
        }
        snackbarCoroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingHabitToDelete by remember { mutableStateOf<Habit?>(null) }
    val onDelete: (Habit) -> Unit = {
        showDeleteDialog = true
        pendingHabitToDelete = it
    }

    val onUnarchive: (Habit) -> Unit = { viewModel.unarchiveHabit(it) }

    ConfirmationDialog(
        showDialog = showDeleteDialog,
        title = stringResource(R.string.archive_delete_title),
        description = stringResource(R.string.archive_delete_description),
        confirmText = stringResource(R.string.archive_delete_confirm),
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            pendingHabitToDelete?.let { viewModel.deleteHabit(it) }
            pendingHabitToDelete = null
            showDeleteDialog = false
        }
    )


    Column {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = { Text(stringResource(R.string.archive_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
                }
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )

        when (habits) {
            is Result.Success -> {
                ArchivedHabitList(
                    (habits as Result.Success<List<HabitWithActions>>).value,
                    onUnarchive,
                    onDelete
                )
            }
            Result.Loading -> {}
            is Result.Failure -> ErrorView(
                label = stringResource(R.string.dashboard_error),
                modifier = Modifier.statusBarsPadding()
            )
        }
    }
}

@Composable
fun ArchivedHabitList(
    habits: List<HabitWithActions>,
    onUnarchive: (Habit) -> Unit,
    onDelete: (Habit) -> Unit
) {
    LazyColumn {
        items(habits) {
            Text(text = it.habit.name)
            Button(onClick = { onUnarchive(it.habit) }) {
                Text("Unarchive")
            }
            Button(onClick = { onDelete(it.habit) }) {
                Text("Delete")
            }
        }
    }
}