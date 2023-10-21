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

import android.app.Application
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.feature.misc.settings.AppInfo
import com.ofalvai.habittracker.feature.widgets.base.WidgetUpdater
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {

    @Provides
    @Singleton
    fun provideAppInfo() = AppInfo(
        versionName = BuildConfig.VERSION_NAME,
        buildType = BuildConfig.BUILD_TYPE,
        appId = BuildConfig.APPLICATION_ID,
        urlPrivacyPolicy = BuildConfig.URL_PRIVACY_POLICY,
        urlSourceCode = BuildConfig.URL_SOURCE_CODE
    )

    @Provides
    @Singleton
    @AppCoroutineScope
    fun provideAppCoroutineScope() = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideWidgetUpdater(
        app: Application,
        habitDao: HabitDao,
        @AppCoroutineScope scope: CoroutineScope
    ) = WidgetUpdater(app, habitDao, scope)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppCoroutineScope
