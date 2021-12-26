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

import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.ContentWithPlaceholder
import com.ofalvai.habittracker.ui.common.ConfirmationDialog
import com.ofalvai.habittracker.ui.common.ErrorView
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.common.asEffect
import com.ofalvai.habittracker.ui.model.ArchivedHabit
import com.ofalvai.habittracker.ui.theme.AppIcons
import com.ofalvai.habittracker.ui.theme.AppTextStyle
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
    var pendingHabitToDelete by remember { mutableStateOf<ArchivedHabit?>(null) }
    val onDelete: (ArchivedHabit) -> Unit = {
        showDeleteDialog = true
        pendingHabitToDelete = it
    }

    val onUnarchive: (ArchivedHabit) -> Unit = { viewModel.unarchiveHabit(it) }

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
                    (habits as Result.Success<List<ArchivedHabit>>).value,
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
private fun ArchivedHabitList(
    habits: List<ArchivedHabit>,
    onUnarchive: (ArchivedHabit) -> Unit,
    onDelete: (ArchivedHabit) -> Unit
) {
    ContentWithPlaceholder(
        showPlaceholder = habits.isEmpty(),
        placeholder = { EmptyView() }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp),
        ) {
            items(habits) {
                ArchivedHabitItem(it, onUnarchive, onDelete)
            }
        }
    }
}

@Composable
private fun ArchivedHabitItem(
    habit: ArchivedHabit,
    onUnarchive: (ArchivedHabit) -> Unit,
    onDelete: (ArchivedHabit) -> Unit
) {
    Card(
        elevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                text = habit.name,
                style = AppTextStyle.habitSubtitle
            )
            HabitSummary(habit)
            Row(Modifier.padding(top = 16.dp)) {
                OutlinedButton(onClick = { onUnarchive(habit) }) {
                    Text(stringResource(R.string.archive_action_unarchive))
                }
                Spacer(Modifier.width(16.dp))
                OutlinedButton(onClick = { onDelete(habit) }) {
                    Text(stringResource(R.string.archive_action_delete))
                }
            }
        }
    }
}

@Composable
private fun HabitSummary(habit: ArchivedHabit) {
    val lastActionLabel = if (habit.lastAction != null) {
        DateUtils.getRelativeTimeSpanString(
            habit.lastAction.toEpochMilli(),
            System.currentTimeMillis(),
            0
        )
    } else {
        stringResource(R.string.archive_habit_last_action_never)
    }
    val mergedLabel = stringResource(
        R.string.archive_habit_summary,
        lastActionLabel,
        habit.totalActionCount
    )

    Text(
        text = mergedLabel,
        style = MaterialTheme.typography.subtitle2
    )
}

@Composable
private fun EmptyView() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp)) {
        Icon(
            modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally).alpha(0.5f),
            painter = AppIcons.Archive,
            contentDescription = stringResource(R.string.common_archive)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            text = stringResource(R.string.archive_empty),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
    }
}