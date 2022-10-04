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

import app.cash.turbine.test
import com.ofalvai.habittracker.core.common.StreamOpener
import com.ofalvai.habittracker.core.common.Telemetry
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.database.entity.Action
import com.ofalvai.habittracker.core.database.entity.Habit
import com.ofalvai.habittracker.core.testing.MainCoroutineRule
import com.ofalvai.habittracker.feature.misc.export.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.time.Instant

class ExportViewModelTest {

    private val dao = mock<HabitDao>()
    private val telemetry = mock<Telemetry>()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `Given data summary When app data changes Then summary is updated`() = runTest(UnconfinedTestDispatcher()) {
        // Given
        val habitCountFlow = MutableSharedFlow<Int>(replay = 0)
        val actionsFlow = MutableSharedFlow<List<Action>>(replay = 0)
        given(dao.getTotalHabitCount()).willReturn(habitCountFlow)
        given(dao.getAllActions()).willReturn(actionsFlow)

        // When
        val viewModel = viewModel()

        // Then
        viewModel.dataSummary.test {
            habitCountFlow.emit(0)
            actionsFlow.emit(emptyList())

            val expectedInitialSummary = DataSummary(
                habitCount = 0,
                actionCount = 0,
                lastActivity = null
            )
            assertEquals(expectedInitialSummary, awaitItem())

            habitCountFlow.emit(1)
            val lastTimestamp = Instant.now()
            actionsFlow.emit(listOf(
                Action(
                    id = 1,
                    habit_id = 1,
                    timestamp = lastTimestamp.minusSeconds(3)
                ),
                Action(
                    id = 2,
                    habit_id = 1,
                    timestamp = lastTimestamp
                )
            ))
            val expectedUpdatedSummary = DataSummary(
                habitCount = 1,
                actionCount = 2,
                lastActivity = lastTimestamp
            )
            skipItems(1) // There is an emission where habit count is 1, but actions are still not updated
            assertEquals(expectedUpdatedSummary, awaitItem())
        }
    }

    @Test
    fun `Given file URI When backing up Then state is correct`() = runTest {
        // Given
        val streamOpener = object : StreamOpener {
            override fun openInputStream(uri: URI): InputStream {
                return ByteArrayInputStream(ByteArray(0))
            }

            override fun openOutputStream(uri: URI): OutputStream {
                return ByteArrayOutputStream()
            }

        }
        val viewModel = viewModel(streamOpener)
        val habit = Habit(id = 1, name = "Meditation", color = Habit.Color.Blue, order = 0, archived = false, notes = "")
        val backupFileURI = URI.create("mock")
        given(dao.getHabits()).willReturn(listOf(habit))
        given(dao.getAllActions()).willReturn(flowOf(emptyList()))

        viewModel.exportState.test {
            assertEquals(ExportState(null, null), awaitItem())

            // When
            viewModel.exportToFile(backupFileURI)

            // Then
            assertEquals(ExportState(backupFileURI, null), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `Given backup with higher backup version When importing backup Then error is raised`() = runTest {
        // Given
        val streamOpener = object : StreamOpener {
            override fun openInputStream(uri: URI): InputStream {
                val content = BackupContent(
                    habitsCSV = CSVHandler.exportHabitList(emptyList()).toString(),
                    actionsCSV = CSVHandler.exportActionList(emptyList()).toString(),
                    metadata = BackupContent.Metadata(backupVersion = 74)
                )
                val output = ByteArrayOutputStream()
                ArchiveHandler.writeBackup(output, content)
                return ByteArrayInputStream(output.toByteArray())
            }

            override fun openOutputStream(uri: URI): OutputStream {
                return ByteArrayOutputStream()
            }

        }
        val viewModel = viewModel(streamOpener)
        val backupFileURI = URI.create("mock")

        viewModel.importState.test {
            assertEquals(ImportState(null, null, null), awaitItem())

            // When
            viewModel.importFromFile(backupFileURI)

            // Then
            val expectedError = ExportImportError.BackupVersionTooHigh
            assertEquals(ImportState(backupFileURI, null, expectedError), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `Given backup file When opening Then view state is correct`() = runTest {
        // Given
        val actionTimestamp = Instant.now()
        val streamOpener = object : StreamOpener {
            override fun openInputStream(uri: URI): InputStream {
                val habits = listOf(Habit(id = 1, name = "Meditation", color = Habit.Color.Blue, order = 0, archived = false, notes = ""))
                val actions = listOf(Action(id = 1, habit_id = 1, timestamp = actionTimestamp))
                val content = BackupContent(
                    habitsCSV = CSVHandler.exportHabitList(habits).toString(),
                    actionsCSV = CSVHandler.exportActionList(actions).toString(),
                    metadata = BackupContent.Metadata(backupVersion = 1)
                )
                val output = ByteArrayOutputStream()
                ArchiveHandler.writeBackup(output, content)
                return ByteArrayInputStream(output.toByteArray())
            }

            override fun openOutputStream(uri: URI): OutputStream {
                return ByteArrayOutputStream()
            }

        }
        val viewModel = viewModel(streamOpener)
        val backupFileURI = URI.create("mock")

        viewModel.importState.test {
            assertEquals(ImportState(null, null, null), awaitItem())

            // When
            viewModel.importFromFile(backupFileURI)

            // Then
            val expectedSummary = DataSummary(
                habitCount = 1,
                actionCount = 1,
                // Converting to CSV and reading it back loses some precision
                lastActivity = Instant.ofEpochMilli(actionTimestamp.toEpochMilli())
            )
            assertEquals(ImportState(backupFileURI, expectedSummary, null), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `Given opened backup When restoring it Then state is updated`() = runTest {
        // Given
        val actionTimestamp = Instant.now()
        val streamOpener = object : StreamOpener {
            override fun openInputStream(uri: URI): InputStream {
                val habits = listOf(Habit(id = 1, name = "Meditation", color = Habit.Color.Blue, order = 0, archived = false, notes = ""))
                val actions = listOf(Action(id = 1, habit_id = 1, timestamp = actionTimestamp))
                val content = BackupContent(
                    habitsCSV = CSVHandler.exportHabitList(habits).toString(),
                    actionsCSV = CSVHandler.exportActionList(actions).toString(),
                    metadata = BackupContent.Metadata(backupVersion = 1)
                )
                val output = ByteArrayOutputStream()
                ArchiveHandler.writeBackup(output, content)
                return ByteArrayInputStream(output.toByteArray())
            }

            override fun openOutputStream(uri: URI): OutputStream {
                return ByteArrayOutputStream()
            }

        }
        val viewModel = viewModel(streamOpener)
        val backupFileURI = URI.create("mock")

        viewModel.importState.test {
            assertEquals(
                ImportState(null, null, null),
                awaitItem()
            )
            viewModel.importFromFile(backupFileURI)
            skipItems(1)

            // When
            viewModel.onRestoreBackup(backupFileURI)

            // Then
            assertEquals(
                ImportState(null, null, null),
                awaitItem()
            )

            ensureAllEventsConsumed()
        }
    }

    private fun viewModel(streamOpener: StreamOpener = mock()) = ExportViewModel(
        streamOpener = streamOpener,
        habitDao = dao,
        telemetry = telemetry
    )
}