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

package com.ofalvai.habittracker.core.common

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

// TODO: fix uncommented tests (broken due to https://github.com/cashapp/turbine/issues/113)
class OnboardingTest {

    private val appPreferences = mock<AppPreferences>()

    @Test
    fun `Given empty app state When initialized Then state is first step`() = runTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(false)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            val expected = OnboardingState(step = OnboardingData.steps[0], totalSteps = OnboardingData.totalSteps)
            assertEquals(expected, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `Given completed onboarding When initialized Then state is null`() = runTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(true)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(true)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(true)
        given(appPreferences.onboardingInsightsOpened).willReturn(true)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            assertNull(awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `Given empty app state When each step is completed Then state is persisted and updated`() = runTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(false)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            val expected1 = OnboardingState(step = OnboardingData.steps[0], totalSteps = OnboardingData.totalSteps)
            assertEquals(expected1, awaitItem())

            onboardingManager.firstHabitCreated()
//            val expected2 = expected1.copy(step = OnboardingData.steps[1])
//            assertEquals(expected2, awaitItem())
            verify(appPreferences).onboardingFirstHabitCreated = true

            onboardingManager.firstActionCompleted()
//            val expected3 = expected1.copy(step = OnboardingData.steps[2])
//            assertEquals(expected3, awaitItem())
            verify(appPreferences).onboardingFirstActionCompleted = true

            onboardingManager.habitDetailsOpened()
//            val expected4 = expected1.copy(step = OnboardingData.steps[3])
//            assertEquals(expected4, awaitItem())
            verify(appPreferences).onboardingHabitDetailsOpened = true

            onboardingManager.insightsOpened()
//            assertNull(awaitItem())
            verify(appPreferences).onboardingInsightsOpened = true

            cancelAndConsumeRemainingEvents()

        }
    }

    @Test
    fun `Given first habit created When initialized Then state is second step`() = runTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(true)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            val expected = OnboardingState(step = OnboardingData.steps[1], totalSteps = OnboardingData.totalSteps)
            assertEquals(expected, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `Given first habit created but action not completed When initialized Then state is second step`() = runTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(true)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            val expected = OnboardingState(step = OnboardingData.steps[1], totalSteps = OnboardingData.totalSteps)
            assertEquals(expected, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `Given empty app state When opening habit details before completing first action Then state is correctly updated`() = runTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(false)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            val expected1 = OnboardingState(step = OnboardingData.steps[0], totalSteps = OnboardingData.totalSteps)
            assertEquals(expected1, awaitItem())

            onboardingManager.firstHabitCreated()
//            val expected2 = expected1.copy(step = OnboardingData.steps[1])
//            assertEquals(expected2, awaitItem())
            verify(appPreferences).onboardingFirstHabitCreated = true

            // Habit detail is opened before action completion
            onboardingManager.habitDetailsOpened()
//            val expected3 = expected1.copy(step = OnboardingData.steps[1])
//            assertEquals(expected3, awaitItem())
            verify(appPreferences).onboardingHabitDetailsOpened = true

            // Completing action, skipping the complete action step in onboarding
            onboardingManager.firstActionCompleted()
//            val expected4 = expected1.copy(step = OnboardingData.steps[3])
//            assertEquals(expected4, awaitItem())
            verify(appPreferences).onboardingFirstActionCompleted = true

            expectNoEvents()
        }
    }

    @Test
    fun `Given first habit created When insights opened before anything else Then state is correctly updated`() = runTest {
        // Given
        given(appPreferences.onboardingFirstHabitCreated).willReturn(true)
        given(appPreferences.onboardingFirstActionCompleted).willReturn(false)
        given(appPreferences.onboardingHabitDetailsOpened).willReturn(false)
        given(appPreferences.onboardingInsightsOpened).willReturn(false)
        val onboardingManager = OnboardingManager(appPreferences)

        // Then
        onboardingManager.state.test {
            val expected = OnboardingState(step = OnboardingData.steps[1], totalSteps = OnboardingData.totalSteps)
            assertEquals(expected, awaitItem())

            // Insights opened before action completion
            onboardingManager.insightsOpened()
//            assertEquals(expected.copy(step = OnboardingData.steps[1]), awaitItem())
            verify(appPreferences).onboardingInsightsOpened = true

            onboardingManager.firstActionCompleted()
//            assertEquals(expected.copy(step = OnboardingData.steps[2]), awaitItem())
            verify(appPreferences).onboardingFirstActionCompleted = true

            // Opening habit details, next state is null because Insights is already visited
            onboardingManager.habitDetailsOpened()
//            assertNull(awaitItem())
            verify(appPreferences).onboardingHabitDetailsOpened = true

            expectNoEvents()
        }
    }
}