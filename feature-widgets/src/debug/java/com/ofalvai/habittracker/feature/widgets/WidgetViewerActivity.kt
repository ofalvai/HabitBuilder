/*
 * Copyright 2023 Olivér Falvai
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

package com.ofalvai.habittracker.feature.widgets

import android.app.Application
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.google.android.glance.tools.viewer.GlanceSnapshot
import com.google.android.glance.tools.viewer.GlanceViewerActivity
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitDayView
import com.ofalvai.habittracker.feature.widgets.today.TodayData
import com.ofalvai.habittracker.feature.widgets.today.TodayWidget
import com.ofalvai.habittracker.feature.widgets.today.TodayWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
@AndroidEntryPoint
class WidgetViewerActivity : GlanceViewerActivity() {

    @Inject lateinit var app: Application
    @Inject lateinit var habitDao: HabitDao

    override suspend fun getGlanceSnapshot(
        receiver: Class<out GlanceAppWidgetReceiver>
    ): GlanceSnapshot {
        return when (receiver) {
            TodayWidgetReceiver::class.java -> GlanceSnapshot(
                instance = TodayWidget(
                    initialData = TodayData(habits = listOf(
                        HabitDayView(Habit(name = "Meditate", color = Habit.Color.Yellow, notes = ""), toggled = true),
                        HabitDayView(Habit(name = "Exercise for 10 min", color = Habit.Color.Blue, notes = ""), toggled = false),
                        HabitDayView(Habit(name = "Read for 20 min", color = Habit.Color.Red, notes = ""), toggled = false),
                        HabitDayView(Habit(name = "Plan my day", color = Habit.Color.Green, notes = ""), toggled = true)
                    )),
                    application = app,
                    habitDao = habitDao
                )
            )

            else -> throw IllegalArgumentException()
        }
    }

    override fun getProviders() = listOf(TodayWidgetReceiver::class.java)
}