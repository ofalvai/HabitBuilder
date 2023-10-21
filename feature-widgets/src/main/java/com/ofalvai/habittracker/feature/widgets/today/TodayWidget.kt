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

import android.content.Context
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitDayView
import com.ofalvai.habittracker.core.ui.theme.composeColor
import com.ofalvai.habittracker.feature.widgets.GlanceTheme
import com.ofalvai.habittracker.feature.widgets.R
import com.ofalvai.habittracker.feature.widgets.base.AppWidgetRoot
import com.ofalvai.habittracker.feature.widgets.base.clickToMainScreen
import com.ofalvai.habittracker.feature.widgets.base.stringResource
import com.ofalvai.habittracker.feature.widgets.base.toColorInt
import com.ofalvai.habittracker.feature.widgets.toModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint(GlanceAppWidgetReceiver::class)
class TodayWidgetReceiver : Hilt_TodayWidgetReceiver() {

    @Inject
    lateinit var habitDao: HabitDao

    override val glanceAppWidget: GlanceAppWidget get() = TodayWidget(initialData, habitDao)

    @ExperimentalGlanceApi
    override val coroutineContext = Dispatchers.IO
}

data class TodayData(
    val habits: List<HabitDayView>
)

private val initialData = TodayData(emptyList())

class TodayWidget(
    initialData: TodayData,
    private val habitDao: HabitDao
): GlanceAppWidget() {

    private var data by mutableStateOf(initialData)

    private suspend fun loadData(): TodayData {
        val habits = habitDao.getHabitDayViewsAt(LocalDate.now()).map { it.toModel() }
        return TodayData(habits)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        data = loadData()

        provideContent {
            AppWidgetRoot {
                if (data.habits.isEmpty()) {
                    Text(
                        text = stringResource(R.string.empty_state_no_habits),
                        modifier = GlanceModifier.padding(16.dp).fillMaxWidth()
                    )
                } else {
                    HabitList(data.habits)
                }
            }
        }
    }
}

@GlanceComposable
@Composable
private fun HabitList(habits: List<HabitDayView>) {
    LazyColumn(GlanceModifier.padding(horizontal = 16.dp)) {
        // Poor man's contentPadding
        item { Spacer(GlanceModifier.size(12.dp)) }
        items(habits, itemId = { it.habit.id.toLong() }) {
            HabitListItem(toggled = it.toggled, habit = it.habit)
        }
        item { Spacer(GlanceModifier.size(12.dp)) }
    }
}

@GlanceComposable
@Composable
private fun HabitListItem(toggled: Boolean, habit: Habit) {
    Row(
        GlanceModifier.padding(vertical = 2.dp).fillMaxWidth()
            .clickToMainScreen(LocalContext.current),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HabitCircle(toggled, habit)
        Text(
            modifier = GlanceModifier.padding(start = 4.dp).clickToMainScreen(LocalContext.current),
            text = habit.name,
            style = TextStyle(
                ColorProvider(
                    GlanceTheme.colors.onSurfaceVariant,
                    GlanceTheme.colors.onSurfaceVariant
                ),
                fontSize = 12.sp
            ),
            maxLines = 1
        )
    }
}

@GlanceComposable
@Composable
private fun HabitCircle(toggled: Boolean, habit: Habit) {
    // Pure RemoteView implementation until glance.appwidget adds support for rounded corners
    // on all SDK levels

    val circle = RemoteViews(LocalContext.current.packageName, R.layout.action_circle)
    val drawable = LocalContext.current.getDrawable(
        if (toggled) R.drawable.action_circle_toggled else R.drawable.action_circle
    )!!

    DrawableCompat.setTint(drawable, habit.color.composeColor.toColorInt())
    circle.setBitmap(R.id.action_circle, "setImageBitmap", drawable.toBitmap())
    AndroidRemoteViews(circle)
}

