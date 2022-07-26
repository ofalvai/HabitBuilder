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
import android.net.Uri
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
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import java.io.StringReader
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
    ExportFailed
}

data class ExportState(
    val zipUri: Uri?,
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
    val exportState: MutableStateFlow<ExportState> =
        MutableStateFlow(ExportState(zipUri = null, error = null))

    val exportDocumentName
        get() = "HabitTracker-backup-${
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.zip"
    val exportDocumentMimeType = "application/zip"
    val importDocumentFormats = arrayOf(exportDocumentMimeType)
    val createDocumentContract = ActivityResultContracts.CreateDocument(exportDocumentMimeType)
    val openDocumentContract = ActivityResultContracts.OpenDocument()

    fun onCreateDocumentResult(uri: Uri?) {
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
                    appContext.contentResolver.openOutputStream(uri).use {
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

    fun onOpenDocumentResult(uri: Uri?) {
        if (uri == null) {
            // TODO: error handling
        } else {
            viewModelScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val backup = readBackup(uri)
                        if (backup.metadata.backupVersion > BACKUP_VERSION) {
                            throw InvalidBackupException("Backup was created with a newer app version. Update the app to the latest version and try again.")
                        }

                        val habits = CSVHandler.importHabitList(StringReader(backup.habitsCSV))
                        val actions =
                            CSVHandler.importActionList(StringReader(backup.actionsCSV))
                        habitDao.restoreBackup(habits, actions)
                    }
                } catch (e: Exception) {
                    logcat(LogPriority.ERROR) { e.asLog() }
                    // TODO
                }
            }
        }
    }

    private fun readBackup(uri: Uri): BackupContent {
        val inputStream = appContext.contentResolver.openInputStream(uri)
        checkNotNull(inputStream)
        return ArchiveHandler.readBackup(inputStream)
    }

    private fun combineDataSummary(count: Int, actions: List<Action>) = DataSummary(
        habitCount = count,
        actionCount = actions.size,
        lastActivity = actions.lastOrNull()?.timestamp
    )
}