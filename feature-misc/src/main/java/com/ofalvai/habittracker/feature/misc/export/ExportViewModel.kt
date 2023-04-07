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

import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.core.common.StreamOpener
import com.ofalvai.habittracker.core.common.Telemetry
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.database.entity.Action
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.StringReader
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val BACKUP_VERSION = 1

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val streamOpener: StreamOpener,
    private val habitDao: HabitDao,
    private val telemetry: Telemetry
) : ViewModel() {

    val dataSummary: Flow<DataSummary> = habitDao.getTotalHabitCount()
        .combine(habitDao.getAllActions(), ::combineDataSummary)
        .distinctUntilChanged()

    val exportState: MutableStateFlow<ExportState> = MutableStateFlow(ExportState(outputFileURI = null, error = null))

    val importState: MutableStateFlow<ImportState> = MutableStateFlow(ImportState(backupFileURI = null, backupSummary = null, error = null))

    val exportDocumentName
        get() = "HabitTracker-backup-${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}.zip"
    val exportDocumentMimeType = "application/zip"
    val importDocumentFormats = arrayOf(exportDocumentMimeType)
    val createDocumentContract = ActivityResultContracts.CreateDocument(exportDocumentMimeType)
    val openDocumentContract = ActivityResultContracts.OpenDocument()

    fun exportToFile(uri: URI?) {
        if (uri == null) {
            telemetry.logNonFatal(IllegalArgumentException("onCreateDocumentResult: null URI"))
            exportState.value = ExportState(
                outputFileURI = null, error = ExportImportError.FilePickerURIEmpty
            )
            return
        }
        telemetry.leaveBreadcrumb(
            "exportToFile",
            mapOf("uri" to uri.toString()),
            Telemetry.BreadcrumbType.UserAction
        )
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
                    streamOpener.openOutputStream(uri).use {
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

    fun importFromFile(uri: URI?) {
        if (uri == null) {
            telemetry.logNonFatal(IllegalArgumentException("onOpenDocumentResult: null URI"))
            importState.value = ImportState(
                backupFileURI = null, backupSummary = null, error = ExportImportError.FilePickerURIEmpty
            )
            return
        }
        telemetry.leaveBreadcrumb(
            "importFromFile",
            mapOf("uri" to uri.toString()),
            Telemetry.BreadcrumbType.UserAction
        )
        viewModelScope.launch {
            try {
                val backup = loadBackup(uri)
                if (backup.backupVersion > BACKUP_VERSION) {
                    telemetry.logNonFatal(IllegalArgumentException("Backup version in ZIP too high: ${backup.backupVersion}, app defines $BACKUP_VERSION"))
                    importState.value = ImportState(
                        backupFileURI = uri, backupSummary = null, error = ExportImportError.BackupVersionTooHigh
                    )
                    return@launch
                }

                val backupSummary = DataSummary(
                    habitCount = backup.habits.size,
                    actionCount = backup.actions.size,
                    lastActivity = backup.actions.maxByOrNull { it.timestamp.epochSecond }?.timestamp
                )
                importState.value = ImportState(backupFileURI = uri, backupSummary, error = null)
            } catch (e: Exception) {
                telemetry.logNonFatal(e)
                importState.value = ImportState(
                    backupFileURI = null, backupSummary = null, error = ExportImportError.ImportFailed
                )
            }
        }
    }

    fun onRestoreBackup(uri: URI) {
        viewModelScope.launch {
            try {
                val backup = loadBackup(uri)
                habitDao.restoreBackup(backup.habits, backup.actions)
                importState.value = ImportState(backupFileURI = null, backupSummary = null, error = null)
            } catch (e: Exception) {
                telemetry.logNonFatal(e)
                importState.value = ImportState(
                    backupFileURI = null, backupSummary = null, error = ExportImportError.ImportFailed
                )
            }
        }
    }

    private suspend fun loadBackup(uri: URI): BackupData = withContext(Dispatchers.IO) {
        streamOpener.openInputStream(uri).use { inputStream ->
            val backup = ArchiveHandler.readBackup(inputStream)
            val habits = CSVHandler.importHabitList(StringReader(backup.habitsCSV))
            val actions = CSVHandler.importActionList(StringReader(backup.actionsCSV))
            return@withContext BackupData(
                habits = habits,
                actions = actions,
                backupVersion = backup.metadata.backupVersion
            )
        }
    }

    private fun combineDataSummary(habitCount: Int, actions: List<Action>) = DataSummary(
        habitCount = habitCount,
        actionCount = actions.size,
        lastActivity = actions.lastOrNull()?.timestamp
    )
}