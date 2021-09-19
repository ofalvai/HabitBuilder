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

package com.ofalvai.habittracker

import app.cash.turbine.test
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.dashboard.OnboardingManager
import com.ofalvai.habittracker.ui.model.OnboardingState
import com.ofalvai.habittracker.ui.model.OnboardingSteps
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class OnboardingTest {

    private val appPreferences = mock<AppPreferences>()

    @Test
    fun `Given empty app state When initialized Then state is first step`() = runBlockingTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(false)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            val expected = OnboardingState(step = OnboardingSteps[0], totalSteps = OnboardingSteps.size)
            assertEquals(expected, expectItem())
            expectNoEvents()
        }
    }

    @Test
    fun `Given completed onboarding When initialized Then state is null`() = runBlockingTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(true)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(true)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(true)
        given(appPreferences.onboardingInsightsOpened).willReturn(true)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            assertNull(expectItem())
            expectNoEvents()
        }
    }

    @Test
    fun `Given empty app state When each step is completed Then state is persisted and updated`() = runBlockingTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(false)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        launch { // https://github.com/cashapp/turbine/issues/33
            onboardingManager.state.test {
                val expected1 = OnboardingState(step = OnboardingSteps[0], totalSteps = OnboardingSteps.size)
                assertEquals(expected1, expectItem())

                onboardingManager.firstHabitCreated()
                val expected2 = expected1.copy(step = OnboardingSteps[1])
                assertEquals(expected2, expectItem())
                verify(appPreferences).onboardingFirstHabitCreated = true

                onboardingManager.firstActionCompleted()
                val expected3 = expected1.copy(step = OnboardingSteps[2])
                assertEquals(expected3, expectItem())
                verify(appPreferences).onboardingFirstActionCompleted = true

                onboardingManager.habitDetailsOpened()
                val expected4 = expected1.copy(step = OnboardingSteps[3])
                assertEquals(expected4, expectItem())
                verify(appPreferences).onboardingHabitDetailsOpened = true

                onboardingManager.insightsOpened()
                assertNull(expectItem())
                verify(appPreferences).onboardingInsightsOpened = true

                expectNoEvents()
            }
        }
    }

    @Test
    fun `Given first habit created When initialized Then state is second step`() = runBlockingTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(true)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            val expected = OnboardingState(step = OnboardingSteps[1], totalSteps = OnboardingSteps.size)
            assertEquals(expected, expectItem())
            expectNoEvents()
        }
    }

    @Test
    fun `Given first habit created but action not completed When initialized Then state is second step`() = runBlockingTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(true)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            val expected = OnboardingState(step = OnboardingSteps[1], totalSteps = OnboardingSteps.size)
            assertEquals(expected, expectItem())
            expectNoEvents()
        }
    }

    @Test
    fun `Given empty app state When opening habit details before completing first action Then state is correctly updated`() = runBlockingTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(false)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        launch { // https://github.com/cashapp/turbine/issues/33
            onboardingManager.state.test {
                val expected1 = OnboardingState(step = OnboardingSteps[0], totalSteps = OnboardingSteps.size)
                assertEquals(expected1, expectItem())

                onboardingManager.firstHabitCreated()
                val expected2 = expected1.copy(step = OnboardingSteps[1])
                assertEquals(expected2, expectItem())
                verify(appPreferences).onboardingFirstHabitCreated = true

                // Habit detail is opened before action completion
                onboardingManager.habitDetailsOpened()
                val expected3 = expected1.copy(step = OnboardingSteps[1])
                assertEquals(expected3, expectItem())
                verify(appPreferences).onboardingHabitDetailsOpened = true

                // Completing action, skipping the complete action step in onboarding
                onboardingManager.firstActionCompleted()
                val expected4 = expected1.copy(step = OnboardingSteps[3])
                assertEquals(expected4, expectItem())
                verify(appPreferences).onboardingFirstActionCompleted = true

                expectNoEvents()
            }
        }
    }

    @Test
    fun `Given first habit created When insights opened before anything else Then state is correctly updated`() = runBlockingTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(true)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        launch { // https://github.com/cashapp/turbine/issues/33
            onboardingManager.state.test {
                val expected = OnboardingState(step = OnboardingSteps[1], totalSteps = OnboardingSteps.size)
                assertEquals(expected, expectItem())

                // Insights opened before action completion
                onboardingManager.insightsOpened()
                assertEquals(expected.copy(step = OnboardingSteps[1]), expectItem())
                verify(appPreferences).onboardingInsightsOpened = true

                onboardingManager.firstActionCompleted()
                assertEquals(expected.copy(step = OnboardingSteps[2]), expectItem())
                verify(appPreferences).onboardingFirstActionCompleted = true

                // Opening habit details, next state is null because Insights is already visited
                onboardingManager.habitDetailsOpened()
                assertNull(expectItem())
                verify(appPreferences).onboardingHabitDetailsOpened = true

                expectNoEvents()
            }
        }
    }
}