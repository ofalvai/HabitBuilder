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
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.ui.theme.composeColor
import com.ofalvai.habittracker.feature.widgets.AppWidgetBox
import com.ofalvai.habittracker.feature.widgets.GlanceTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

class TodayWidget(
    private val viewModel: TodayWidgetViewModel?
) : GlanceAppWidget() {
    @Composable
    @GlanceComposable
    override fun Content() {
        GlanceTheme {
            AppWidgetBox() {
                HabitList(viewModel)
            }
        }
    }
}

@Composable
private fun HabitList(viewModel: TodayWidgetViewModel?) {
    val habitState: MutableStateFlow<List<Habit>> = MutableStateFlow(listOf(
        Habit(id = 1, name = "Meditation", color = Habit.Color.Red, notes = ""),
        Habit(id = 2, name = "Test", color = Habit.Color.Yellow, notes = ""),
        Habit(id = 3, name = "Reading", color = Habit.Color.Yellow, notes = ""),
        Habit(id = 4, name = "Touch grass", color = Habit.Color.Blue, notes = ""),
        Habit(id = 5, name = "Exercise", color = Habit.Color.Green, notes = ""),
    ))
    val habits by habitState.collectAsState()
//    val habits = listOf(
//        Habit(id = 1, name = "Meditation", color = Habit.Color.Red, notes = ""),
//        Habit(id = 2, name = "Test", color = Habit.Color.Yellow, notes = ""),
//        Habit(id = 3, name = "Reading", color = Habit.Color.Yellow, notes = ""),
//        Habit(id = 4, name = "Touch grass", color = Habit.Color.Blue, notes = ""),
//        Habit(id = 5, name = "Exercise", color = Habit.Color.Green, notes = ""),
//    )
    HabitCircle(toggled = Random.nextBoolean(), habit = Habit(id = 1, name = "Meditation", color = Habit.Color.Red, notes = ""))
    LazyColumn {
        items(habits, itemId = { it.id.toLong() }) {
            HabitCircle(toggled = Random.nextBoolean(), habit = it)
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

@AndroidEntryPoint
class TodayWidgetReceiver : GlanceAppWidgetReceiver() {

//    @Inject lateinit var viewModel: TodayWidgetViewModel

    override val glanceAppWidget = TodayWidget(null)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }
}
