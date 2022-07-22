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

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.core.database.HabitDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val BACKUP_VERSION = 1

private class BackupContent(
    val habitsCSV: String,
    val actionsCSV: String,
    val metadata: Metadata
) {
    class Metadata(val backupVersion: Int)
}

class ExportViewModel(
    @SuppressLint("StaticFieldLeak") private val appContext: Context,
    private val habitDao: HabitDao
) : ViewModel() {

    val exportDocumentName = "HabitTracker-backup.zip" // TODO: include timestamp
    val importDocumentFormats = arrayOf("application/zip")
    val createDocumentContract = ActivityResultContracts.CreateDocument("application/zip")
    val openDocumentContract = ActivityResultContracts.OpenDocument()

    fun onCreateDocumentResult(uri: Uri?) {
        if (uri == null) {
            // TODO: error handling
        } else {
            viewModelScope.launch {
                val habits = habitDao.getHabits()
                val actions = habitDao.getAllActions()

                val habitsCsv = CSVHandler.exportHabitList(habits)
                val actionsCsv = CSVHandler.exportActionList(actions)

                val backupContent = BackupContent(
                    habitsCSV = habitsCsv.toString(),
                    actionsCSV = actionsCsv.toString(),
                    metadata = BackupContent.Metadata(backupVersion = BACKUP_VERSION)
                )
                writeFileContents(uri, backupContent)
            }
        }
    }

    fun onOpenDocumentResult(uri: Uri?) {
        if (uri == null) {
            // TODO: error handling
        } else {
            viewModelScope.launch {
                val backup = readBackup(uri)
                val habits = CSVHandler.importHabitList(StringReader(backup.habitsCSV))
                val actions = CSVHandler.importActionList(StringReader(backup.actionsCSV))

                if (backup.metadata.backupVersion > BACKUP_VERSION) {
                    // TODO: handle error
                }

                habitDao.deleteAllHabits()

            }
        }
    }

    private suspend fun writeFileContents(uri: Uri, content: BackupContent) {
        withContext(Dispatchers.IO) {
            try {
                appContext.contentResolver.openOutputStream(uri).use {
                    checkNotNull(it)
                    createZip(it, content)
                }
                appContext.contentResolver.openFileDescriptor(uri, "w")?.use {
                }
            } catch (e: FileNotFoundException) {
                logcat("ExportScreen", LogPriority.ERROR) { e.asLog() }
            } catch (e: IOException) {
                logcat("ExportScreen", LogPriority.ERROR) { e.asLog() }
            }
        }
    }

    private fun createZip(output: OutputStream, content: BackupContent) {
        ZipOutputStream(output).use { outputStream ->
            outputStream.putNextEntry(ZipEntry("habits.csv"))
            outputStream.write(content.habitsCSV.toByteArray())
            outputStream.closeEntry()

            outputStream.putNextEntry(ZipEntry("actions.csv"))
            outputStream.write(content.actionsCSV.toByteArray())
            outputStream.closeEntry()

            outputStream.putNextEntry(ZipEntry("metadata.txt"))
            outputStream.write("backup_version=${content.metadata.backupVersion}".toByteArray())
            outputStream.closeEntry()
        }
    }

    private fun readBackup(uri: Uri): BackupContent {
        val habitsStream = ByteArrayOutputStream()
        val actionsStream = ByteArrayOutputStream()
        val metadataStream = ByteArrayOutputStream()

        appContext.contentResolver.openInputStream(uri).use {
            checkNotNull(it)
            ZipInputStream(it).use { inputStream ->
                var zipEntry = inputStream.nextEntry

                while (zipEntry != null) {
                    if (!zipEntry.isDirectory) {
                        when (zipEntry.name) {
                            "habits.csv" -> inputStream.copyTo(habitsStream)
                            "actions.csv" -> inputStream.copyTo(actionsStream)
                            "metadata.txt" -> inputStream.copyTo(metadataStream)
                        }
                    }

                    zipEntry = inputStream.nextEntry
                }
            }
        }

        val metadataContent = metadataStream.toString()
        val backupVersion = metadataContent
            .split("=")
            .lastOrNull()
            ?.toIntOrNull()
            ?: throw InvalidBackupException("Couldn't find backup version in metadata.txt. File contents: $metadataContent")

        return BackupContent(
            habitsCSV = habitsStream.toString(),
            actionsCSV = actionsStream.toString(),
            metadata = BackupContent.Metadata(backupVersion)
        )
    }
}

class InvalidBackupException(message: String) : IllegalArgumentException(message)