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

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import com.ofalvai.habittracker.core.common.AndroidStreamOpener
import com.ofalvai.habittracker.core.common.AppPreferences
import com.ofalvai.habittracker.core.common.StreamOpener
import com.ofalvai.habittracker.core.common.Telemetry
import com.ofalvai.habittracker.core.common.TelemetryImpl
import com.ofalvai.habittracker.core.database.AppDatabase
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.database.MIGRATIONS
import com.ofalvai.habittracker.feature.misc.settings.AppInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import logcat.logcat

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {
    @Provides
    fun provideDb(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "app-db")
            .setQueryCallback(::roomQueryLogCallback, Runnable::run)
            .addMigrations(*MIGRATIONS)
            .build()
    }

    @Provides
    fun provideHabitDao(db: AppDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideSharedPreferences(app: Application): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)

    @Provides
    fun provideTelemetry(app: Application, appPreferences: AppPreferences): Telemetry = TelemetryImpl(app, appPreferences)

    @Provides
    fun provideAppInfo() = AppInfo(
        versionName = BuildConfig.VERSION_NAME,
        buildType = BuildConfig.BUILD_TYPE,
        appId = BuildConfig.APPLICATION_ID,
        urlPrivacyPolicy = BuildConfig.URL_PRIVACY_POLICY,
        urlSourceCode = BuildConfig.URL_SOURCE_CODE
    )

    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    fun provideStreamOpener(app: Application): StreamOpener = AndroidStreamOpener(app)
}

private fun roomQueryLogCallback(sqlQuery: String, bindArgs: List<Any>) {
    logcat("RoomQueryLog") { "Query: $sqlQuery" }
    if (bindArgs.isNotEmpty()) {
        logcat("RoomQueryLog") { "Args: $bindArgs" }
    }
}
