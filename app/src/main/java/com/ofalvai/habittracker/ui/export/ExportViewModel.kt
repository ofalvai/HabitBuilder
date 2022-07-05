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
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private const val BACKUP_VERSION = 1

class ExportViewModel(
    @SuppressLint("StaticFieldLeak") private val appContext: Context,
    private val habitDao: HabitDao
) : ViewModel() {

    fun getDocumentName(): String {
        return "HabitTracker-backup.zip" // TODO: include timestamp
    }

    fun getCreateDocumentContract() = ActivityResultContracts.CreateDocument("application/zip")

    fun onCreateDocumentResult(uri: Uri?) {
        if (uri == null) {
            // TODO: error handling
        } else {
            viewModelScope.launch {
                val habits = habitDao.getHabits()
                val actions = habitDao.getAllActions()

                val habitsCsv = CSVHandler.exportHabitList(habits)
                val actionsCsv = CSVHandler.exportActionList(actions)

                writeFileContents(uri, habitsCsv.toString(), actionsCsv.toString())
            }
        }
    }

    private suspend fun writeFileContents(uri: Uri, habitsCsv: String, actionsCsv: String) {
        withContext(Dispatchers.IO) {
            try {
                appContext.contentResolver.openFileDescriptor(uri, "w")?.use {
                    createZip(it.fileDescriptor, habitsCsv, actionsCsv)
                }
            } catch (e: FileNotFoundException) {
                logcat("ExportScreen", LogPriority.ERROR) { e.asLog() }
            } catch (e: IOException) {
                logcat("ExportScreen", LogPriority.ERROR) { e.asLog() }
            }
        }
    }

    private fun createZip(fileDescriptor: FileDescriptor, habitsCsv: String, actionsCsv: String) {
        ZipOutputStream(FileOutputStream(fileDescriptor)).use { outputStream ->
            outputStream.putNextEntry(ZipEntry("habits.csv"))
            outputStream.write(habitsCsv.toByteArray())
            outputStream.closeEntry()

            outputStream.putNextEntry(ZipEntry("actions.csv"))
            outputStream.write(actionsCsv.toByteArray())
            outputStream.closeEntry()

            outputStream.putNextEntry(ZipEntry("metadata.txt"))
            outputStream.write("backup_version=$BACKUP_VERSION".toByteArray())
            outputStream.closeEntry()

        }
    }


}