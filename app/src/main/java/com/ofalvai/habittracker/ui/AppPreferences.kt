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

package com.ofalvai.habittracker.ui

import android.content.SharedPreferences
import com.ofalvai.habittracker.ui.model.DashboardConfig

private const val KEY_DASHBOARD_CONFIG = "dashboard_config"
private const val KEY_ONBOARDING_FIRST_HABIT_CREATED = "onboarding_first_habit_created"
private const val KEY_ONBOARDING_FIRST_ACTION_COMPLETED = "onboarding_first_action_completed"
private const val KEY_ONBOARDING_HABIT_DETAILS_OPENED = "onboarding_habit_details_opened"
private const val KEY_ONBOARDING_INSIGHTS_OPENED = "onboarding_insights_opened"

class AppPreferences(
    private val sharedPreferences: SharedPreferences
) {

    var dashboardConfig: DashboardConfig
        get() {
            val stringValue = sharedPreferences.getString(
                KEY_DASHBOARD_CONFIG, DashboardConfig.FiveDay.toString()
            )!!
            return DashboardConfig.valueOf(stringValue)
        }
        set(value) {
            sharedPreferences.edit().putString(KEY_DASHBOARD_CONFIG, value.toString()).apply()
        }

    var onboardingFirstHabitCreated: Boolean
        get() = sharedPreferences.getBoolean(KEY_ONBOARDING_FIRST_HABIT_CREATED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_ONBOARDING_FIRST_HABIT_CREATED, value).apply()

    var onboardingFirstActionCompleted: Boolean
        get() = sharedPreferences.getBoolean(KEY_ONBOARDING_FIRST_ACTION_COMPLETED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_ONBOARDING_FIRST_ACTION_COMPLETED, value).apply()

    var onboardingHabitDetailsOpened: Boolean
        get() = sharedPreferences.getBoolean(KEY_ONBOARDING_HABIT_DETAILS_OPENED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_ONBOARDING_HABIT_DETAILS_OPENED, value).apply()

    var onboardingInsightsOpened: Boolean
        get() = sharedPreferences.getBoolean(KEY_ONBOARDING_INSIGHTS_OPENED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_ONBOARDING_INSIGHTS_OPENED, value).apply()
}