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