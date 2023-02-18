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

package com.ofalvai.habittracker.feature.misc.archive

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.ui.component.AppDefaultAppBar
import com.ofalvai.habittracker.core.ui.component.ConfirmationDialog
import com.ofalvai.habittracker.core.ui.component.ContentWithPlaceholder
import com.ofalvai.habittracker.core.ui.component.ErrorView
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.core.ui.state.asEffect
import com.ofalvai.habittracker.core.ui.theme.AppTextStyle
import com.ofalvai.habittracker.core.ui.theme.CoreIcons
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.feature.misc.R
import com.ofalvai.habittracker.feature.misc.archive.model.ArchivedHabit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import java.time.Instant
import com.ofalvai.habittracker.core.ui.R as coreR

@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    snackbarHostState: SnackbarHostState,
    navigateBack: () -> Unit
) {
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
            snackbarHostState.showSnackbar(message)
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingHabitToDelete by remember { mutableStateOf<ArchivedHabit?>(null) }
    val onDeleteRequest: (ArchivedHabit) -> Unit = {
        showDeleteDialog = true
        pendingHabitToDelete = it
    }
    val onUnarchive: (ArchivedHabit) -> Unit = { viewModel.unarchiveHabit(it) }
    val onDeleteDismiss: () -> Unit = {
        showDeleteDialog = false
    }
    val onDeleteConfirm: () -> Unit = {
        pendingHabitToDelete?.let { viewModel.deleteHabit(it) }
        pendingHabitToDelete = null
        showDeleteDialog = false
    }

    ArchiveScreen(
        habits,
        showDeleteDialog,
        onDeleteDismiss,
        onDeleteConfirm,
        navigateBack,
        onUnarchive,
        onDeleteRequest
    )
}

@Composable
private fun ArchiveScreen(
    habits: Result<ImmutableList<ArchivedHabit>>,
    showDeleteDialog: Boolean,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onBack: () -> Unit,
    onUnarchive: (ArchivedHabit) -> Unit,
    onDeleteRequest: (ArchivedHabit) -> Unit,
) {
    ConfirmationDialog(
        showDialog = showDeleteDialog,
        title = stringResource(R.string.archive_delete_title),
        description = stringResource(R.string.archive_delete_description),
        confirmText = stringResource(R.string.archive_delete_confirm),
        onDismiss = onDeleteDismiss,
        onConfirm = onDeleteConfirm
    )


    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        AppDefaultAppBar(
            title = { Text(stringResource(R.string.archive_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, stringResource(coreR.string.common_back))
                }
            }
        )

        when (habits) {
            is Result.Success -> {
                ArchivedHabitList(
                    habits.value,
                    onUnarchive,
                    onDeleteRequest
                )
            }
            Result.Loading -> {}
            is Result.Failure -> ErrorView(
                label = stringResource(R.string.archive_load_error)
            )
        }
    }
}

@Composable
private fun ArchivedHabitList(
    habits: ImmutableList<ArchivedHabit>,
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
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                text = habit.name,
                style = AppTextStyle.habitTitle
            )
            HabitSummary(habit)
            Row(Modifier.padding(top = 16.dp)) {
                Button(onClick = { onUnarchive(habit) }) {
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
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun EmptyView() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp)) {
        Icon(
            modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally).alpha(0.5f),
            painter = CoreIcons.Archive,
            contentDescription = stringResource(coreR.string.common_archive)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            text = stringResource(R.string.archive_empty),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@ShowkaseComposable(name = "Screen", group = "Archive", styleName = "Empty")
@Composable
fun PreviewArchiveScreenEmpty() {
    PreviewTheme {
        ArchiveScreen(
            habits = Result.Success(persistentListOf()),
            showDeleteDialog = false,
            onDeleteDismiss = {},
            onDeleteConfirm = {},
            onBack = {},
            onUnarchive = {},
            onDeleteRequest = {}
        )
    }
}

@Preview
@ShowkaseComposable(name = "Screen", group = "Archive", styleName = "Items")
@Composable
fun PreviewArchiveScreenItems() {
    PreviewTheme {
        val items = persistentListOf(
            ArchivedHabit(id = 1, name = "Meditation", totalActionCount = 45, lastAction = Instant.ofEpochMilli(1624563468000)),
            ArchivedHabit(id = 2, name = "Yoga", totalActionCount = 0, lastAction = null)
        )
        ArchiveScreen(
            habits = Result.Success(items),
            showDeleteDialog = false,
            onDeleteDismiss = {},
            onDeleteConfirm = {},
            onBack = {},
            onUnarchive = {},
            onDeleteRequest = {}
        )
    }
}