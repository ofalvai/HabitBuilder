/*
 * Copyright 2021 Olivér Falvai
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

package com.ofalvai.habittracker.composable

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ofalvai.habittracker.ui.MainActivity
import com.ofalvai.habittracker.ui.dashboard.AddHabitForm
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import com.ofalvai.habittracker.util.BaseInstrumentedTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddHabitTest : BaseInstrumentedTest() {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    // https://github.com/mockito/mockito-kotlin/issues/272
    open class OnSaveCallback: (Habit) -> Unit {
        override fun invoke(p1: Habit) = Unit
    }

    @Test
    fun whenLaunched_ThenNameIsFocused() {
        val onSave: (Habit) -> Unit = {}

        composeRule.setContent {
            HabitTrackerTheme {
                AddHabitForm(onSave)
            }
        }

        composeRule.onNodeWithText("Habit name").assertIsFocused()
    }

    @Test
    fun givenEmptyName_WhenCreateClicked_ThenErrorMessageVisible() {
        val onSave: (Habit) -> Unit = {}

        composeRule.setContent {
            HabitTrackerTheme {
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
    fun givenName_WhenCreateClicked_ThenSaveAndNavigate() {
        val expectedHabitName = "Test habit name"
        var onSaveCalled = false
        val onSave: (Habit) -> Unit = {
            onSaveCalled = true
            assertEquals(expectedHabitName, it.name)
        }

        composeRule.setContent {
            HabitTrackerTheme {
                AddHabitForm(onSave)
            }
        }

        composeRule.apply {
            onNodeWithText("Habit name").performTextInput(expectedHabitName)
            onNodeWithText("Create habit").performClick()
            assertTrue(onSaveCalled)
        }
    }
}