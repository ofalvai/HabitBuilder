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

package com.ofalvai.habittracker.ui.dashboard

import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.model.OnboardingState
import com.ofalvai.habittracker.ui.model.OnboardingSteps
import kotlinx.coroutines.flow.MutableStateFlow

class OnboardingManager(
    private val appPreferences: AppPreferences
) {
    private var firstHabitCreated by appPreferences::onboardingFirstHabitCreated
    private var firstActionCompleted by appPreferences::onboardingFirstActionCompleted
    private var habitDetailsOpened by appPreferences::onboardingHabitDetailsOpened
    private var insightsOpened by appPreferences::onboardingInsightsOpened

    /**
     * Current onboarding state or null if onboarding has been completed
     */
    val state = MutableStateFlow(currentState())

    fun firstHabitCreated() {
        if (!firstHabitCreated) {
            firstHabitCreated = true
        }
        state.value = currentState()
    }

    fun firstActionCompleted() {
        if (!firstActionCompleted) {
            firstActionCompleted = true
        }
        state.value = currentState()
    }

    fun habitDetailsOpened() {
        if (!habitDetailsOpened) {
            habitDetailsOpened = true
        }
        state.value = currentState()
    }

    fun insightsOpened() {
        if (!insightsOpened) {
            insightsOpened = true
        }
        state.value = currentState()
    }

    private fun currentState(): OnboardingState? {
        return if (!firstHabitCreated) {
            OnboardingState(step = OnboardingSteps[0], totalSteps = OnboardingSteps.size)
        } else if (!firstActionCompleted) {
            OnboardingState(step = OnboardingSteps[1], totalSteps = OnboardingSteps.size)
        } else if (!habitDetailsOpened) {
            OnboardingState(step = OnboardingSteps[2], totalSteps = OnboardingSteps.size)
        } else if (!insightsOpened) {
            OnboardingState(step = OnboardingSteps[3], totalSteps = OnboardingSteps.size)
        } else {
            null
        }
    }
}