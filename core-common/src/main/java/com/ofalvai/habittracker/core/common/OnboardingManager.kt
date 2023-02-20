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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

object OnboardingData {
    val steps: ImmutableList<Step> = persistentListOf(
            Step(
                index = 0,
                title = R.string.onboarding_step_create_title,
                subtitle = R.string.onboarding_step_create_subtitle
            ),
            Step(
                index = 1,
                title = R.string.onboarding_step_longpress_title,
                subtitle = R.string.onboarding_step_longpress_subtitle
            ),
            Step(
                index = 2,
                title = R.string.onboarding_step_details_title,
                subtitle = R.string.onboarding_step_details_subtitle
            ),
            Step(
                index = 3,
                title = R.string.onboarding_step_insights_title,
                subtitle = R.string.onboarding_step_insights_subtitle
            )
        )
    val totalSteps = steps.size
}

@Singleton
class OnboardingManager @Inject constructor(
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
            OnboardingState(step = OnboardingData.steps[0], totalSteps = OnboardingData.totalSteps)
        } else if (!firstActionCompleted) {
            OnboardingState(step = OnboardingData.steps[1], totalSteps = OnboardingData.totalSteps)
        } else if (!habitDetailsOpened) {
            OnboardingState(step = OnboardingData.steps[2], totalSteps = OnboardingData.totalSteps)
        } else if (!insightsOpened) {
            OnboardingState(step = OnboardingData.steps[3], totalSteps = OnboardingData.totalSteps)
        } else {
            null
        }
    }
}