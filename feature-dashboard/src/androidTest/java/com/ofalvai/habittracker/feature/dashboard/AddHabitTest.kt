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

package com.ofalvai.habittracker.feature.dashboard

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.testing.BaseInstrumentedTest
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.feature.dashboard.ui.addhabit.AddHabitForm
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AddHabitTest : BaseInstrumentedTest() {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    // https://github.com/mockito/mockito-kotlin/issues/272
    open class OnSaveCallback : (Habit) -> Unit {
        override fun invoke(p1: Habit) = Unit
    }

    @Test
    fun whenLaunched_ThenNameIsFocused() {
        val onSave: (Habit) -> Unit = {}

        composeRule.setContent {
            PreviewTheme {
                AddHabitForm(onSave)
            }
        }

        composeRule.onNodeWithText("Habit name").assertIsFocused()
    }

    @Test
    fun givenEmptyName_WhenCreateClicked_ThenErrorMessageVisible() {
        val onSave: (Habit) -> Unit = {}

        composeRule.setContent {
            PreviewTheme {
                AddHabitForm(onSave)
            }
        }

        composeRule.apply {
            onNodeWithText("Create habit").performClick()
            onNodeWithText("Habit name").assertIsFocused()
            onNodeWithText("Enter a name for new habit").assertExists("TextField error message not found")
        }
    }

    @Test
    fun givenNameAndNotes_WhenCreateClicked_ThenSaveAndNavigate() {
        val expectedHabitName = "Test habit name"
        val expectedNotes = "Test habit notes\nSecond line"
        var onSaveCalled = false
        val onSave: (Habit) -> Unit = {
            onSaveCalled = true
            assertEquals(expectedHabitName, it.name)
            assertEquals(expectedNotes, it.notes)
        }

        composeRule.setContent {
            PreviewTheme {
                AddHabitForm(onSave)
            }
        }

        composeRule.apply {
            onNodeWithText("Habit name").performTextInput(expectedHabitName)
            onNodeWithText("Habit name").performImeAction()
            onNodeWithText("Notes").assertIsFocused().performTextInput(expectedNotes)
            onNodeWithText("Create habit").performClick()
            assertTrue(onSaveCalled)
        }
    }
}
