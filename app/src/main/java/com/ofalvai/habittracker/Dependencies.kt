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

import android.preference.PreferenceManager
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.ofalvai.habittracker.core.common.AndroidStreamOpener
import com.ofalvai.habittracker.core.common.AppPreferences
import com.ofalvai.habittracker.core.common.OnboardingManager
import com.ofalvai.habittracker.core.common.TelemetryImpl
import com.ofalvai.habittracker.core.database.AppDatabase
import com.ofalvai.habittracker.core.database.MIGRATIONS
import com.ofalvai.habittracker.feature.insights.ui.InsightsViewModel
import com.ofalvai.habittracker.feature.misc.archive.ArchiveViewModel
import com.ofalvai.habittracker.feature.misc.export.ExportViewModel
import com.ofalvai.habittracker.feature.misc.settings.AppInfo
import com.ofalvai.habittracker.feature.misc.settings.LicensesViewModel
import com.ofalvai.habittracker.feature.misc.settings.SettingsViewModel
import com.ofalvai.habittracker.repo.ActionRepository
import com.ofalvai.habittracker.ui.dashboard.AddHabitViewModel
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailViewModel
import logcat.logcat

object Dependencies {

    private val appContext = HabitTrackerApplication.INSTANCE.applicationContext

    private val db = Room.databaseBuilder(appContext, AppDatabase::class.java, "app-db")
        .setQueryCallback(::roomQueryLogCallback, Runnable::run)
        .addMigrations(*MIGRATIONS)
        .build()

    val dao = db.habitDao()

    private val actionRepository = ActionRepository(dao)

    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(HabitTrackerApplication.INSTANCE)

    private val appPreferences = AppPreferences(sharedPreferences)

    private val onboardingManager = OnboardingManager(appPreferences)

    val telemetry = TelemetryImpl(appContext, appPreferences)

    private val streamOpener = AndroidStreamOpener(appContext)

    private val appInfo = AppInfo(
        versionName = BuildConfig.VERSION_NAME,
        buildType = BuildConfig.BUILD_TYPE,
        appId = BuildConfig.APPLICATION_ID,
        urlPrivacyPolicy = BuildConfig.URL_PRIVACY_POLICY,
        urlSourceCode = BuildConfig.URL_SOURCE_CODE
    )

    val viewModelFactory = viewModelFactory {
        initializer { AddHabitViewModel(dao, onboardingManager, telemetry) }
        initializer {
            DashboardViewModel(dao, actionRepository, appPreferences, telemetry, onboardingManager)
        }
        initializer { HabitDetailViewModel(dao, actionRepository, telemetry, onboardingManager) }
        initializer { InsightsViewModel(dao, telemetry, onboardingManager) }
        initializer { LicensesViewModel(appContext) }
        initializer { ArchiveViewModel(dao, telemetry) }
        initializer {
            SettingsViewModel(appPreferences, appInfo)
        }
        initializer { ExportViewModel(streamOpener, dao, telemetry) }
    }
}

private fun roomQueryLogCallback(sqlQuery: String, bindArgs: List<Any>) {
    logcat("RoomQueryLog") { "Query: $sqlQuery" }
    if (bindArgs.isNotEmpty()) {
        logcat("RoomQueryLog") { "Args: $bindArgs" }
    }
}
