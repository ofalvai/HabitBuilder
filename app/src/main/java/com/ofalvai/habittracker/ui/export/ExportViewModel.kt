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

import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.database.entity.Action
import com.ofalvai.habittracker.telemetry.Telemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.StringReader
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val BACKUP_VERSION = 1

class InvalidBackupException(message: String) : IllegalArgumentException(message)

data class DataSummary(
    val habitCount: Int,
    val actionCount: Int,
    val lastActivity: Instant? // null if there are no actions
)

enum class ExportImportError {
    FilePickerURIEmpty,
    ExportFailed,
    ImportFailed,
    BackupVersionTooHigh
}

data class ExportState(
    val zipUri: URI?,
    val error: ExportImportError?
)

data class ImportState(
    val zipUri: URI?,
    val backupSummary: DataSummary?,
    val error: ExportImportError?
)

// TODO: add tests
class ExportViewModel(
    private val appContext: Context,
    private val habitDao: HabitDao,
    private val telemetry: Telemetry
) : ViewModel() {

    val dataSummary: Flow<DataSummary> = habitDao.getTotalHabitCount()
        .combine(habitDao.getAllActions(), ::combineDataSummary)
        .distinctUntilChanged()

    val exportState: MutableStateFlow<ExportState> = MutableStateFlow(ExportState(zipUri = null, error = null))

    val importState: MutableStateFlow<ImportState> = MutableStateFlow(ImportState(zipUri = null, backupSummary = null, error = null))

    val exportDocumentName
        get() = "HabitTracker-backup-${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}.zip"
    val exportDocumentMimeType = "application/zip"
    val importDocumentFormats = arrayOf(exportDocumentMimeType)
    val createDocumentContract = ActivityResultContracts.CreateDocument(exportDocumentMimeType)
    val openDocumentContract = ActivityResultContracts.OpenDocument()

    fun onCreateDocumentResult(uri: URI?) {
        if (uri == null) {
            telemetry.logNonFatal(IllegalArgumentException("onCreateDocumentResult: null URI"))
            exportState.value = ExportState(
                zipUri = null, error = ExportImportError.FilePickerURIEmpty
            )
            return
        }
        viewModelScope.launch {
            try {
                val habits = habitDao.getHabits()
                val actions = habitDao.getAllActions().first()
                val habitsCsv = CSVHandler.exportHabitList(habits)
                val actionsCsv = CSVHandler.exportActionList(actions)

                val backupContent = BackupContent(
                    habitsCSV = habitsCsv.toString(),
                    actionsCSV = actionsCsv.toString(),
                    metadata = BackupContent.Metadata(backupVersion = BACKUP_VERSION)
                )
                withContext(Dispatchers.IO) {
                    appContext.contentResolver.openOutputStream(uri.toAndroidURI()).use {
                        checkNotNull(it)
                        ArchiveHandler.writeBackup(it, backupContent)
                    }
                }
                exportState.value = ExportState(uri, error = null)
            } catch (e: Throwable) {
                telemetry.logNonFatal(e)
                exportState.value = ExportState(uri, error = ExportImportError.ExportFailed)
            }
        }
    }

    fun onOpenDocumentResult(uri: URI?) {
        if (uri == null) {
            telemetry.logNonFatal(IllegalArgumentException("onOpenDocumentResult: null URI"))
            importState.value = ImportState(
                zipUri = null, backupSummary = null, error = ExportImportError.FilePickerURIEmpty
            )
            return
        }
        viewModelScope.launch {
            try {
                val backup = loadBackup(uri)
                if (backup.backupVersion > BACKUP_VERSION) {
                    telemetry.logNonFatal(IllegalArgumentException("Backup version too high: ${backup.backupVersion}, app defines $BACKUP_VERSION"))
                    importState.value = ImportState(
                        zipUri = null, backupSummary = null, error = ExportImportError.BackupVersionTooHigh
                    )
                }

                val backupSummary = DataSummary(
                    habitCount = backup.habits.size,
                    actionCount = backup.actions.size,
                    lastActivity = backup.actions.maxByOrNull { it.timestamp.epochSecond }?.timestamp
                )
                importState.value = ImportState(zipUri = uri, backupSummary, error = null)
            } catch (e: Exception) {
                telemetry.logNonFatal(e)
                importState.value = ImportState(
                    zipUri = null, backupSummary = null, error = ExportImportError.ImportFailed
                )
            }
        }
    }

    fun onRestoreBackup(uri: URI) {
        viewModelScope.launch {
            try {
                val backup = loadBackup(uri)
                habitDao.restoreBackup(backup.habits, backup.actions)
                importState.value = ImportState(zipUri = null, backupSummary = null, error = null)
            } catch (e: Exception) {
                telemetry.logNonFatal(e)
                importState.value = ImportState(
                    zipUri = null, backupSummary = null, error = ExportImportError.ImportFailed
                )
            }
        }
    }

    private suspend fun loadBackup(uri: URI): BackupData = withContext(Dispatchers.IO) {
        val inputStream = appContext.contentResolver.openInputStream(uri.toAndroidURI())
        checkNotNull(inputStream)
        val backup = ArchiveHandler.readBackup(inputStream)
        val habits = CSVHandler.importHabitList(StringReader(backup.habitsCSV))
        val actions = CSVHandler.importActionList(StringReader(backup.actionsCSV))
        return@withContext BackupData(
            habits = habits,
            actions = actions,
            backupVersion = backup.metadata.backupVersion
        )
    }

    private fun combineDataSummary(habitCount: Int, actions: List<Action>) = DataSummary(
        habitCount = habitCount,
        actionCount = actions.size,
        lastActivity = actions.lastOrNull()?.timestamp
    )

    private fun URI.toAndroidURI() = android.net.Uri.parse(toString())
}