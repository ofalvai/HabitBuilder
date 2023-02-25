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

package com.ofalvai.habittracker.feature.widgets.today

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.unit.ColorProvider
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitDayView
import com.ofalvai.habittracker.core.ui.theme.composeColor
import com.ofalvai.habittracker.feature.widgets.AppWidgetBox
import com.ofalvai.habittracker.feature.widgets.GlanceTheme
import com.ofalvai.habittracker.feature.widgets.base.BaseGlanceAppWidget
import com.ofalvai.habittracker.feature.widgets.toModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class TodayWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject lateinit var application: Application
    @Inject lateinit var habitDao: HabitDao

    override val glanceAppWidget: GlanceAppWidget get() {
        return TodayWidget(application, habitDao).apply { initiateLoad() }
    }
}

data class TodayData(
    val habits: List<HabitDayView>
)

private val initialData = TodayData(emptyList())

class TodayWidget(
    application: Application,
    private val habitDao: HabitDao
) : BaseGlanceAppWidget<TodayData>(initialData, application) {

    override suspend fun loadData(): TodayData {
        val habits = habitDao.getHabitDayViewsAt(LocalDate.now()).map { it.toModel() }
        return TodayData(habits)
    }

    @Composable
    override fun Content(data: TodayData) {
        GlanceTheme {
            AppWidgetBox {
                HabitList(data.habits)
            }
        }
    }
}

@GlanceComposable
@Composable
private fun HabitList(habits: List<HabitDayView>) {
    LazyColumn {
        items(habits, itemId = { it.habit.id.toLong() }) {
            HabitCircle(toggled = it.toggled, habit = it.habit)
        }
    }
}

@Composable
private fun HabitCircle(toggled: Boolean, habit: Habit) {
    Row(
        GlanceModifier.padding(vertical = 4.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .background(habit.color.composeColor)
                .cornerRadius(24.dp)
                .size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // TODO: replace this once GlanceModifier.border() is available
            if (!toggled) {
                Box(
                    GlanceModifier
                        .background(GlanceTheme.colors.background)
                        .cornerRadius(20.dp)
                        .size(20.dp)
                ) {}
            }
        }

        Text(
            modifier = GlanceModifier.padding(start = 4.dp),
            text = habit.name,
            style = TextStyle(
                ColorProvider(
                    GlanceTheme.colors.onBackground,
                    GlanceTheme.colors.onBackground
                ), // TODO
                fontSize = 14.sp
            ),
            maxLines = 1
        )
    }

}

