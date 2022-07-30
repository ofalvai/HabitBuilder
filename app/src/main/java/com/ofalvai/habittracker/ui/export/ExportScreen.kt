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

package com.ofalvai.habittracker.ui.export

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ofalvai.habittracker.Dependencies

val initialSummary = DataSummary(habitCount = 0, actionCount = 0, lastActivity = null)

@Composable
fun ExportScreen(navController: NavController) {
    val viewModel = viewModel<ExportViewModel>(factory = Dependencies.viewModelFactory)

    val summary by viewModel.dataSummary.collectAsState(initial = initialSummary)
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()

    val createDocumentLauncher = rememberLauncherForActivityResult(viewModel.createDocumentContract) {
        viewModel.onCreateDocumentResult(it)
    }
    val openDocumentLauncher = rememberLauncherForActivityResult(viewModel.openDocumentContract) {
        viewModel.onOpenDocumentResult(it)
    }

    Column(modifier = Modifier.statusBarsPadding()) {
        DataSummary(summary)

        val context = LocalContext.current
        Exporter(
            state = exportState,
            onExportClick = { createDocumentLauncher.launch(viewModel.exportDocumentName) },
            onShareClick = { uri ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = viewModel.exportDocumentMimeType
                }
                startActivity(context, Intent.createChooser(shareIntent, null), null)
            }
        )

        Importer(
            state = importState,
            onChooseFileClick = { openDocumentLauncher.launch(viewModel.importDocumentFormats) },
            onConfirmImport = { viewModel.onRestoreBackup(it) }
        )
    }
}

@Composable
private fun DataSummary(summary: DataSummary) {
    Column {
        Text(text = "Habits: ${summary.habitCount}")
        Text(text = "Actions performed: ${summary.actionCount}")
        val lastActivityString = if (summary.lastActivity != null) {
            DateUtils.getRelativeTimeSpanString(
                summary.lastActivity.toEpochMilli(),
                System.currentTimeMillis(),
                0
            )
        } else "-"
        Text(text = "Last activity: $lastActivityString")
    }
}

@Composable
private fun Exporter(
    state: ExportState,
    onExportClick: () -> Unit,
    onShareClick: (Uri) -> Unit
) {
    Button(onClick = onExportClick) {
        Text(text = "Export data")
    }

    if (state.zipUri != null) {
        Button(onClick = { onShareClick(state.zipUri) }) {
            Text(text = "Share")
        }
    }
}

@Composable
private fun Importer(
    state: ImportState,
    onChooseFileClick: () -> Unit,
    onConfirmImport: (Uri) -> Unit
) {
    Column {
        if (state.backupSummary != null) {
            DataSummary(summary = state.backupSummary)
        }
        if (state.zipUri != null) {
            Button(onClick = { onConfirmImport(state.zipUri) }) {
                Text(text = "Restore backup")
            }
        } else {
            Button(onClick = onChooseFileClick) {
                Text(text = "Choose backup")
            }
        }
    }


}

