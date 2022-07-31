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
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.core.ui.component.AppBar
import com.ofalvai.habittracker.core.ui.component.SingleStat
import com.ofalvai.habittracker.core.ui.theme.AppTextStyle
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.core.ui.theme.errorContainer
import com.ofalvai.habittracker.core.ui.theme.surfaceVariant
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.ofalvai.habittracker.core.ui.R as commonR

val initialSummary = DataSummary(habitCount = 0, actionCount = 0, lastActivity = null)

@Composable
fun ExportScreen(navController: NavController) {
    val viewModel = viewModel<ExportViewModel>(factory = Dependencies.viewModelFactory)

    val summary by viewModel.dataSummary.collectAsState(initial = initialSummary)
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()

    val createDocumentLauncher =
        rememberLauncherForActivityResult(viewModel.createDocumentContract) {
            viewModel.onCreateDocumentResult(URI.create(it.toString()))
        }
    val openDocumentLauncher = rememberLauncherForActivityResult(viewModel.openDocumentContract) {
        viewModel.onOpenDocumentResult(URI.create(it.toString()))
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        AppBar(
            title = {
                Text(text = stringResource(R.string.export_title), style = AppTextStyle.screenTitle)
            }
        )

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            DataSummary(summary)

            val context = LocalContext.current
            Exporter(
                modifier = Modifier.padding(vertical = 32.dp),
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
}

@Composable
private fun DataSummary(summary: DataSummary) {
    Column(
        modifier = Modifier
            .surfaceBackground()
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val lastActivityString = if (summary.lastActivity != null) {
            DateUtils.getRelativeTimeSpanString(
                summary.lastActivity.toEpochMilli(),
                System.currentTimeMillis(),
                0
            )
        } else "-"

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SingleStat(
                value = summary.habitCount.toString(),
                label = stringResource(R.string.export_datasummary_habits),
                modifier = Modifier.weight(0.33f)
            )
            SingleStat(
                value = summary.actionCount.toString(),
                label = stringResource(R.string.export_datasummary_actions),
                modifier = Modifier.weight(0.33f)
            )
            SingleStat(
                value = lastActivityString.toString(),
                label = stringResource(R.string.export_datasummary_last_activity),
                modifier = Modifier.weight(0.33f)
            )
        }
    }
}

@Composable
private fun ExportImportError(error: ExportImportError, modifier: Modifier = Modifier) {
    val errorMessage = when(error) {
        ExportImportError.ExportFailed -> stringResource(R.string.export_error_export_failed)
        ExportImportError.FilePickerURIEmpty -> stringResource(R.string.export_error_uri_empty)
        ExportImportError.ImportFailed -> stringResource(R.string.export_error_import_failed)
        ExportImportError.BackupVersionTooHigh -> stringResource(R.string.export_error_backup_version_too_high)
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colors.errorContainer, MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = commonR.string.common_something_went_wrong),
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = errorMessage, style = MaterialTheme.typography.body2)
    }
}

@Composable
private fun Exporter(
    modifier: Modifier = Modifier,
    state: ExportState,
    onExportClick: () -> Unit,
    onShareClick: (URI) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .surfaceBackground()
            .padding(16.dp)
    ) {
        if (state.error != null) {
            ExportImportError(state.error, modifier = Modifier.padding(vertical = 16.dp))
        }

        OutlinedButton(onClick = onExportClick) {
            Text(text = "Export data")
        }

        if (state.zipUri != null) {
            OutlinedButton(onClick = { onShareClick(state.zipUri) }) {
                Text(text = "Share")
            }
        }

    }
}

@Composable
private fun Importer(
    state: ImportState,
    onChooseFileClick: () -> Unit,
    onConfirmImport: (URI) -> Unit
) {
    Column {
        if (state.backupSummary != null) {
            DataSummary(summary = state.backupSummary)
        }
        if (state.zipUri != null) {
            Button(onClick = { onConfirmImport(state.zipUri) }) {
                Text(text = "Restore backup")
            }
        }
        Button(onClick = onChooseFileClick) {
            Text(text = "Choose backup")
        }
    }


}

private fun Modifier.surfaceBackground() = composed {
    this.background(MaterialTheme.colors.surfaceVariant, shape = MaterialTheme.shapes.medium)
}

@Preview
@Composable
fun PreviewDataSummary() {
    val data = DataSummary(
        habitCount = 5,
        actionCount = 74,
        lastActivity = LocalDateTime
            .of(2022, 7, 28, 14, 13)
            .toInstant(ZoneOffset.UTC)
    )
    PreviewTheme {
        DataSummary(data)
    }
}

@Preview
@Composable
fun PreviewExportImportError() {
    PreviewTheme {
        ExportImportError(ExportImportError.BackupVersionTooHigh)
    }
}

