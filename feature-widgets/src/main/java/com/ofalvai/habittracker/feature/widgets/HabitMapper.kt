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

import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitDayView
import com.ofalvai.habittracker.core.database.entity.Habit as HabitEntity
import com.ofalvai.habittracker.core.database.entity.HabitDayView as HabitDayViewEntity

fun HabitDayViewEntity.toModel() = HabitDayView(
    habit = this.habit.toModel(),
    toggled = this.timestamp != null
)

fun HabitEntity.toModel() = Habit(
    id = id,
    name = name,
    color = color.toModelColor(),
    notes = notes
)

fun HabitEntity.Color.toModelColor(): Habit.Color = when (this) {
    HabitEntity.Color.Red -> Habit.Color.Red
    HabitEntity.Color.Green -> Habit.Color.Green
    HabitEntity.Color.Blue -> Habit.Color.Blue
    HabitEntity.Color.Yellow -> Habit.Color.Yellow
    HabitEntity.Color.Cyan -> Habit.Color.Cyan
    HabitEntity.Color.Pink -> Habit.Color.Pink
}