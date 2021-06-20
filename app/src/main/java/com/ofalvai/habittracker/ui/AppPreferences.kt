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
}