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

package com.ofalvai.habittracker.feature.misc

import com.ofalvai.habittracker.feature.misc.export.ArchiveHandler
import com.ofalvai.habittracker.feature.misc.export.BackupContent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

const val CSV_CONTENT_HABITS = """id,name,color,order,archived,notes
1,Meditation,Blue,0,false,
2,Meditation,Yellow,1,false,Notes notes notes
3,Meditation,Red,3,true,"Multi-line 
 description"
"""
const val CSV_CONTENT_ACTIONS = """id,habit_id,timestamp

1,1,1656878248000

2,1,1656878248000

3,2,0

"""

class ArchiveHandlerTest {

    @Test
    fun `Given zip archive When it is read Then contents are correctly parsed`() {
        // Given
        val zip = createTestZip()

        // When
        val content = ArchiveHandler.readBackup(zip)

        // Then
        assertEquals(BackupContent.Metadata(backupVersion = 1), content.metadata)
        assertEquals(CSV_CONTENT_HABITS, content.habitsCSV)
        assertEquals(CSV_CONTENT_ACTIONS, content.actionsCSV)
    }

    @Test
    fun `Given backup content When it is written to a zip archive Then file contents are correct`() {
        // Given
        val expectedContent = BackupContent(
            habitsCSV = CSV_CONTENT_HABITS,
            actionsCSV = CSV_CONTENT_ACTIONS,
            metadata = BackupContent.Metadata(backupVersion = 1)
        )

        // When
        val output = ByteArrayOutputStream()
        ArchiveHandler.writeBackup(output, expectedContent)

        // Then
        val content = readTestZip(ByteArrayInputStream(output.toByteArray()))
        assertEquals(expectedContent, content)

    }

    private fun createTestZip(): InputStream {
        val content = BackupContent(
            habitsCSV = CSV_CONTENT_HABITS,
            actionsCSV = CSV_CONTENT_ACTIONS,
            metadata = BackupContent.Metadata(backupVersion = 1)
        )

        val output = ByteArrayOutputStream()

        ZipOutputStream(output).use { outputStream ->
            outputStream.putNextEntry(ZipEntry("habits.csv"))
            outputStream.write(content.habitsCSV.toByteArray())
            outputStream.closeEntry()

            outputStream.putNextEntry(ZipEntry("actions.csv"))
            outputStream.write(content.actionsCSV.toByteArray())
            outputStream.closeEntry()

            outputStream.putNextEntry(ZipEntry("metadata.txt"))
            val metadataContent = """
                |backup_version=${content.metadata.backupVersion}
                |second_line=test
                |third line
                |
            """.trimMargin()
            outputStream.write(metadataContent.toByteArray())
            outputStream.closeEntry()

            outputStream.putNextEntry(ZipEntry("subfolder/unexpected file.bin"))
            outputStream.write("I'll cause problems on purpose".toByteArray())
            outputStream.closeEntry()
        }

        return ByteArrayInputStream(output.toByteArray())
    }

    private fun readTestZip(input: InputStream): BackupContent {
        val habitsStream = ByteArrayOutputStream()
        val actionsStream = ByteArrayOutputStream()
        val metadataStream = ByteArrayOutputStream()

        input.use {
            ZipInputStream(it).use { inputStream ->
                var zipEntry = inputStream.nextEntry
                while (zipEntry != null) {
                    if (zipEntry.isDirectory) {
                        zipEntry = inputStream.nextEntry
                        continue
                    }
                    when (zipEntry.name) {
                        "habits.csv" -> inputStream.copyTo(habitsStream)
                        "actions.csv" -> inputStream.copyTo(actionsStream)
                        "metadata.txt" -> inputStream.copyTo(metadataStream)
                    }

                    zipEntry = inputStream.nextEntry
                }
            }
        }

        val metadataContent = metadataStream.toString()
        val backupVersion = metadataContent
            .split("\n")
            .firstOrNull { it.startsWith("backup_version") }
            ?.split("=")
            ?.lastOrNull()
            ?.toIntOrNull()
            ?: throw IllegalStateException("Couldn't find backup version in metadata.txt. File contents: $metadataContent")

        return BackupContent(
            habitsCSV = habitsStream.toString(),
            actionsCSV = actionsStream.toString(),
            metadata = BackupContent.Metadata(backupVersion)
        )
    }
}