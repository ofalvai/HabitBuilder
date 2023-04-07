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

package com.ofalvai.habittracker.feature.widgets.base

import android.app.Application
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.feature.widgets.today.TodayWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

private val UPDATABLE_WIDGETS = listOf(
    TodayWidgetReceiver::class.java
)

class WidgetUpdater @Inject constructor(
    private val app: Application,
    habitDao: HabitDao,
    externalScope: CoroutineScope
) {
    init {
        externalScope.launch {
            habitDao.getActiveHabitsWithActions().collect { notifyWidgets() }
        }
    }

    private fun notifyWidgets() {
        UPDATABLE_WIDGETS.forEach {
            val intent = Intent(app, it).apply {
                action = GlanceAppWidgetReceiver.ACTION_DEBUG_UPDATE
            }
            app.sendBroadcast(intent)
        }
    }
}