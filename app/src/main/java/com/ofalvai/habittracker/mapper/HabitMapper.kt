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

package com.ofalvai.habittracker.mapper

import com.ofalvai.habittracker.ui.model.ArchivedHabit
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import com.ofalvai.habittracker.persistence.entity.Habit as HabitEntity
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

fun mapHabitEntityToModel(habitsWithActions: List<HabitWithActionsEntity>): List<HabitWithActions> {
    return habitsWithActions.map {
        HabitWithActions(
            habit = Habit(it.habit.id, it.habit.name, it.habit.color.toUIColor()),
            actions = actionsToRecentDays(it.actions),
            totalActionCount = it.actions.size,
            actionHistory = actionsToHistory(it.actions)
        )
    }
}

fun mapHabitEntityToArchivedModel(habitsWithActions: List<HabitWithActionsEntity>): List<ArchivedHabit> {
    return habitsWithActions.map {
        ArchivedHabit(
            id = it.habit.id,
            name = it.habit.name,
            totalActionCount = it.actions.size,
            lastAction = it.actions.lastOrNull()?.timestamp
        )
    }
}

fun Habit.toEntity(order: Int, archived: Boolean) = HabitEntity(
    id = this.id,
    name = this.name,
    color = this.color.toEntityColor(),
    order = order,
    archived = archived
)

fun HabitEntity.Color.toUIColor(): Habit.Color = when (this) {
    HabitEntity.Color.Red -> Habit.Color.Red
    HabitEntity.Color.Green -> Habit.Color.Green
    HabitEntity.Color.Blue -> Habit.Color.Blue
    HabitEntity.Color.Yellow -> Habit.Color.Yellow
}

fun Habit.Color.toEntityColor(): HabitEntity.Color = when (this) {
    Habit.Color.Red -> HabitEntity.Color.Red
    Habit.Color.Green -> HabitEntity.Color.Green
    Habit.Color.Blue -> HabitEntity.Color.Blue
    Habit.Color.Yellow -> HabitEntity.Color.Yellow
}