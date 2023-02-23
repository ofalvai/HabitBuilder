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

import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class TodayWidgetViewModel @Inject constructor(
    private val dao: HabitDao
) {

    val habits: MutableStateFlow<List<Habit>> = MutableStateFlow(listOf(
        Habit(id = 1, name = "Meditation", color = Habit.Color.Red, notes = ""),
        Habit(id = 2, name = "Test", color = Habit.Color.Yellow, notes = ""),
        Habit(id = 3, name = "Reading", color = Habit.Color.Yellow, notes = ""),
        Habit(id = 4, name = "Touch grass", color = Habit.Color.Blue, notes = ""),
        Habit(id = 5, name = "Exercise", color = Habit.Color.Green, notes = ""),
    ))

    init {
    }

}