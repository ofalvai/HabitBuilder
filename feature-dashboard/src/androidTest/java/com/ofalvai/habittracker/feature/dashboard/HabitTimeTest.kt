/*
 * Copyright 2025 Olivér Falvai
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

package com.ofalvai.habittracker.feature.dashboard

import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.ActionHistory
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.core.testing.BaseInstrumentedTest
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.feature.dashboard.ui.addhabit.AddHabitForm
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.fiveday.FiveDayHabitList
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.fiveday.HabitCard as FiveDayHabitCard
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.minicalendar.HabitCard as MiniCalendarHabitCard

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class HabitTimeTest : BaseInstrumentedTest() {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun whenLaunched_ThenTimeButtonIsVisible() {
        val onSave: (Habit) -> Unit = {}

        composeRule.setContent {
            PreviewTheme {
                AddHabitForm(onSave)
            }
        }

        composeRule.onNodeWithText("Set time (optional)").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Clear").assertDoesNotExist()
    }

    @Test
    fun givenInitialTime_WhenLaunched_ThenTimeIsDisplayedAndClearButtonVisible() {
        val onSave: (Habit) -> Unit = {}
        val initialTime = LocalTime.of(14, 30)
        val formattedTime = initialTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

        composeRule.setContent {
            PreviewTheme {
                AddHabitForm(onSave, initialTime)
            }
        }

        composeRule.onNodeWithText(formattedTime).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Clear").assertIsDisplayed()
    }

    @Test
    fun givenInitialTime_WhenClearClicked_ThenTimeIsRemoved() {
        val onSave: (Habit) -> Unit = {}
        val initialTime = LocalTime.of(14, 30)

        composeRule.setContent {
            PreviewTheme {
                AddHabitForm(onSave, initialTime)
            }
        }

        composeRule.onNodeWithContentDescription("Clear").performClick()

        composeRule.onNodeWithText("Set time (optional)").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Clear").assertDoesNotExist()
    }

    @Test
    fun whenHabitSavedWithTime_ThenOnSaveCalledWithCorrectTime() {
        var savedHabit: Habit? = null
        val onSave: (Habit) -> Unit = { savedHabit = it }
        val initialTime = LocalTime.of(9, 15)

        composeRule.setContent {
            PreviewTheme {
                AddHabitForm(onSave, initialTime)
            }
        }

        // Enter habit name
        composeRule.onNodeWithText("Habit name").performTextInput("Morning routine")

        // Click save
        composeRule.onNodeWithText("Create habit").performClick()

        // Verify saved habit has correct time
        assertEquals(initialTime, savedHabit?.time)
    }

    @Test
    fun whenHabitSavedWithoutTime_ThenOnSaveCalledWithNullTime() {
        var savedHabit: Habit? = null
        val onSave: (Habit) -> Unit = { savedHabit = it }

        composeRule.setContent {
            PreviewTheme {
                AddHabitForm(onSave)
            }
        }

        composeRule.onNodeWithText("Habit name").performTextInput("Evening routine")
        composeRule.onNodeWithText("Create habit").performClick()

        assertNull(savedHabit?.time)
    }

    @Test
    fun givenHabitWithTime_WhenDisplayed_ThenTimeIsShown() {
        // Given
        val time = LocalTime.of(14, 30)
        val habit = Habit(
            id = 1,
            name = "Test Habit",
            color = Habit.Color.Blue,
            notes = "",
            time = time
        )
        val formattedTime = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(time)

        // When
        composeRule.setContent {
            PreviewTheme {
                FiveDayHabitCard(
                    habit = habit,
                    actions = persistentListOf(),
                    totalActionCount = 0,
                    actionHistory = ActionHistory.Clean,
                    onActionToggle = { _, _, _ -> },
                    onDetailClick = { },
                    dragOffset = 0f
                )
            }
        }

        // Then
        composeRule.onNodeWithText(formattedTime).assertIsDisplayed()
    }

    @Test
    fun givenHabitWithTime_WhenDisplayedInMiniCalendar_ThenTimeIsShown() {
        // Given
        val time = LocalTime.of(14, 30)
        val habit = Habit(
            id = 1,
            name = "Test Habit",
            color = Habit.Color.Blue,
            notes = "",
            time = time
        )
        val formattedTime = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(time)

        // MiniCalendarHabitCard requires 30 actions for the calendar grid
        val dummyActions = (1..30).map {
            Action(
                id = it,
                toggled = false,
                timestamp = null
            )
        }.toPersistentList()

        // When
        composeRule.setContent {
            PreviewTheme {
                MiniCalendarHabitCard(
                    habit = habit,
                    actions = dummyActions,
                    onActionToggle = { _, _, _ -> },
                    onDetailClick = { },
                    dragOffset = 0f
                )
            }
        }

        // Then
        composeRule.onNodeWithText(formattedTime).assertIsDisplayed()
    }

    @Test
    fun whenTimeButtonClicked_ThenTimePickerDialogAppearsAndUpdatesTime() {
        val onSave: (Habit) -> Unit = {}

        composeRule.setContent {
            PreviewTheme {
                AddHabitForm(onSave)
            }
        }

        // Click the "Set time" button
        composeRule.onNodeWithText("Set time (optional)").performClick()

        // Initialize UiDevice
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Find the "OK" button in the dialog
        val okButton = device.findObject(UiSelector().text("OK"))

        // Wait for dialog to appear and click OK
        okButton.waitForExists(5000)
        okButton.click()

        // After clicking OK, the time should be set.
        // The "Set time (optional)" text should be gone, replaced by a time string.
        composeRule.onNodeWithText("Set time (optional)").assertDoesNotExist()

        // The Clear button should now be visible
        composeRule.onNodeWithContentDescription("Clear").assertIsDisplayed()
    }

    @Test
    fun givenMixedHabits_WhenDisplayed_ThenOnlyHabitWithTimeShowsTime() {
        // Given
        val time = LocalTime.of(14, 30)
        val formattedTime = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(time)

        val habitWithTime = Habit(
            id = 1,
            name = "Habit with time",
            color = Habit.Color.Blue,
            notes = "",
            time = time
        )
        val habitWithoutTime = Habit(
            id = 2,
            name = "Habit without time",
            color = Habit.Color.Red,
            notes = "",
            time = null
        )

        val habits = persistentListOf(
            HabitWithActions(habitWithTime, persistentListOf(), 0, ActionHistory.Clean),
            HabitWithActions(habitWithoutTime, persistentListOf(), 0, ActionHistory.Clean)
        )

        // When
        composeRule.setContent {
            PreviewTheme {
                FiveDayHabitList(
                    habits = habits,
                    onActionToggle = { _, _, _ -> },
                    onHabitClick = { },
                    onAddHabitClick = { },
                    onMove = { }
                )
            }
        }

        // Then
        composeRule.onNodeWithText("Habit with time").assertIsDisplayed()
        composeRule.onNodeWithText(formattedTime).assertIsDisplayed()
        composeRule.onAllNodesWithText(formattedTime).assertCountEquals(1)

        composeRule.onNodeWithText("Habit without time").assertIsDisplayed()
    }
}
