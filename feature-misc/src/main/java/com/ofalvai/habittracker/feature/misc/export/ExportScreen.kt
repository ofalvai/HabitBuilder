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

package com.ofalvai.habittracker.feature.misc.export

import android.content.Intent
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.ui.component.AppDefaultAppBar
import com.ofalvai.habittracker.core.ui.component.SingleStat
import com.ofalvai.habittracker.core.ui.theme.CoreIcons
import com.ofalvai.habittracker.core.ui.theme.LocalAppColors
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.feature.misc.R
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.ofalvai.habittracker.core.ui.R as commonR

val initialSummary = DataSummary(habitCount = 0, actionCount = 0, lastActivity = null)

@Composable
fun ExportScreen(viewModel: ExportViewModel, navigateBack: () -> Unit) {
    val summary by viewModel.dataSummary.collectAsState(initial = initialSummary)
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()

    val createDocumentLauncher =
        rememberLauncherForActivityResult(viewModel.createDocumentContract) {
            viewModel.exportToFile(URI.create(it.toString()))
        }
    val openDocumentLauncher = rememberLauncherForActivityResult(viewModel.openDocumentContract) {
        viewModel.importFromFile(URI.create(it.toString()))
    }
    val context = LocalContext.current
    val onShareClick: (URI) -> Unit =  {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(it.toString()))
            type = viewModel.exportDocumentMimeType
        }
        startActivity(context, Intent.createChooser(shareIntent, null), null)
    }

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()
    ) {
        AppDefaultAppBar(
            title = { Text(text = stringResource(R.string.export_title)) },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.Rounded.ArrowBack, stringResource(commonR.string.common_back))
                }
            }
        )

        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
        ) {
            BackupInfo(Modifier.padding(vertical = 16.dp))

            DataSummary(summary)

            Exporter(
                modifier = Modifier.padding(vertical = 32.dp),
                state = exportState,
                onExportClick = { createDocumentLauncher.launch(viewModel.exportDocumentName) },
                onShareClick = onShareClick
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SingleStat(
                value = summary.habitCount.toString(),
                label = stringResource(R.string.export_datasummary_habits),
                modifier = Modifier.weight(0.5f)
            )
            SingleStat(
                value = summary.actionCount.toString(),
                label = stringResource(R.string.export_datasummary_actions),
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}

@Composable
private fun ExportImportError(error: ExportImportError, modifier: Modifier = Modifier) {
    val errorMessage = when (error) {
        ExportImportError.ExportFailed -> stringResource(R.string.export_error_export_failed)
        ExportImportError.FilePickerURIEmpty -> stringResource(R.string.export_error_uri_empty)
        ExportImportError.ImportFailed -> stringResource(R.string.export_error_import_failed)
        ExportImportError.BackupVersionTooHigh -> stringResource(R.string.export_error_backup_version_too_high)
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = commonR.string.common_something_went_wrong),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BackupInfo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(LocalAppColors.current.successContainer, shape = MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(
            painter = CoreIcons.InfoOutlined,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.export_backup_info),
            style = MaterialTheme.typography.bodySmall
        )
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
        Text(
            text = stringResource(R.string.export_widget_export_title),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(R.string.export_widget_export_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        AnimatedVisibility(visible = state.error != null) {
            ExportImportError(state.error!!, modifier = Modifier.padding(vertical = 16.dp))
        }

        OutlinedButton(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onExportClick
        ) {
            Text(text = stringResource(R.string.export_widget_export_filepicker_button))
        }

        AnimatedVisibility(visible = state.outputFileURI != null) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .background(LocalAppColors.current.successContainer, MaterialTheme.shapes.medium)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.export_widget_export_success_title),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = stringResource(
                            R.string.export_widget_export_success_path,
                            state.outputFileURI!!.path
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(onClick = { onShareClick(state.outputFileURI) }) {
                        Text(text = stringResource(R.string.export_widget_export_success_share_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun Importer(
    modifier: Modifier = Modifier,
    state: ImportState,
    onChooseFileClick: () -> Unit,
    onConfirmImport: (URI) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .surfaceBackground()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.export_widget_import_title),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(R.string.export_widget_import_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        AnimatedVisibility(visible = state.error != null) {
            ExportImportError(state.error!!, modifier = Modifier.padding(vertical = 16.dp))
        }

        AnimatedVisibility(visible = state.backupSummary != null) {
            BackupSummary(state = state)
        }

        AnimatedVisibility(visible = state.backupFileURI != null) {
            Button(onClick = { onConfirmImport(state.backupFileURI!!) }) {
                Text(text = stringResource(R.string.export_widget_import_restore_button))
            }
        }

        OutlinedButton(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onChooseFileClick
        ) {
            Text(text = stringResource(R.string.export_widget_import_filepicker_button))
        }
    }
}

@Composable
private fun BackupSummary(state: ImportState) {
    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
        if (state.backupFileURI != null) {
            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = buildAnnotatedString {
                    withStyle(boldStyle) {
                        append(stringResource(R.string.export_widget_import_file_path))
                    }
                    append(" ")
                    append(state.backupFileURI.path)
                },
            )
        }
        if (state.backupSummary != null) {
            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = buildAnnotatedString {
                    withStyle(boldStyle) {
                        append(stringResource(R.string.export_widget_import_habit_count))
                    }
                    append(" ")
                    append(state.backupSummary.habitCount.toString())
                    append("\n")
                    withStyle(boldStyle) {
                        append(stringResource(R.string.export_widget_import_action_count))
                    }
                    append(" ")
                    append(state.backupSummary.actionCount.toString())
                    append("\n")
                    withStyle(boldStyle) {
                        append(stringResource(R.string.export_widget_import_last_activity))
                    }
                    val lastActivityString = if (state.backupSummary.lastActivity != null) {
                        DateUtils.getRelativeTimeSpanString(
                            state.backupSummary.lastActivity.toEpochMilli(),
                            System.currentTimeMillis(),
                            0
                        )
                    } else "-"
                    append(" ")
                    append(lastActivityString.toString())

                }
            )
        }
    }
}

private fun Modifier.surfaceBackground() = composed {
    this.background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
}

@Preview
@ShowkaseComposable(name = "Data summary", group = "Backup and restore")
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
@ShowkaseComposable(name = "Error", group = "Backup and restore")
fun PreviewExportImportError() {
    PreviewTheme {
        ExportImportError(ExportImportError.BackupVersionTooHigh)
    }
}

@Preview
@Composable
@ShowkaseComposable("Importer", group = "Backup and restore")
fun PreviewImporter() {
    PreviewTheme {
        val state = ImportState(
            backupFileURI = URI.create("documents/HabitTracker-export.zip"),
            backupSummary = DataSummary(
                habitCount = 5,
                actionCount = 34,
                lastActivity = Instant.now()
            ),
            error = null
        )
        Importer(
            state = state,
            onChooseFileClick = { },
            onConfirmImport = {})
    }
}

@Preview
@Composable
@ShowkaseComposable("Exporter", group = "Backup and restore")
fun PreviewExporter() {
    PreviewTheme {
        val state = ExportState(
            outputFileURI = URI.create("documents/HabitTracker-export.zip"),
            error = null
        )
        Exporter(
            state = state,
            onExportClick = { },
            onShareClick = {})
    }
}