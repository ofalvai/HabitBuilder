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

@file:Suppress("DEPRECATION")

package com.ofalvai.habittracker

import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.ofalvai.habittracker.persistence.AppDatabase
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.persistence.MIGRATIONS
import com.ofalvai.habittracker.telemetry.Telemetry
import com.ofalvai.habittracker.telemetry.TelemetryImpl
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.dashboard.AddHabitViewModel
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailViewModel
import com.ofalvai.habittracker.ui.insights.InsightsViewModel
import com.ofalvai.habittracker.ui.settings.LicensesViewModel
import timber.log.Timber

object Dependencies {

    private val appContext = HabitTrackerApplication.INSTANCE.applicationContext

    private val db = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "app-db"
    )
        .setQueryCallback(::roomQueryLogCallback, Runnable::run)
        .addMigrations(*MIGRATIONS)
        .build()

    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(HabitTrackerApplication.INSTANCE)

    private val appPreferences = AppPreferences(sharedPreferences)

    val telemetry = TelemetryImpl(appContext)

    val viewModelFactory = AppViewModelFactory(db.habitDao(), appPreferences, appContext, telemetry)
}

private fun roomQueryLogCallback(sqlQuery: String, bindArgs: List<Any>) {
    Timber.tag("RoomQueryLog")
    Timber.d("Query: %s", sqlQuery)
    if (bindArgs.isNotEmpty()) {
        Timber.tag("RoomQueryLog")
        Timber.d("Args: %s", bindArgs.toString())
    }
}

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val habitDao: HabitDao,
    private val appPreferences: AppPreferences,
    private val appContext: Context,
    private val telemetry: Telemetry
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (AddHabitViewModel::class.java.isAssignableFrom(modelClass)) {
            return AddHabitViewModel(habitDao) as T
        }
        if (DashboardViewModel::class.java.isAssignableFrom(modelClass)) {
            return DashboardViewModel(habitDao, appPreferences, telemetry) as T
        }
        if (HabitDetailViewModel::class.java.isAssignableFrom(modelClass)) {
            return HabitDetailViewModel(habitDao, telemetry) as T
        }
        if (InsightsViewModel::class.java.isAssignableFrom(modelClass)) {
            return InsightsViewModel(habitDao, telemetry) as T
        }
        if (LicensesViewModel::class.java.isAssignableFrom(modelClass)) {
            return LicensesViewModel(appContext) as T
        }
        throw IllegalArgumentException("No matching ViewModel for ${modelClass.canonicalName}")
    }
}