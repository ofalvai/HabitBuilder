package com.ofalvai.habittracker.mapper

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

fun Habit.toEntity() = HabitEntity(
    id = this.id,
    name = this.name,
    color = this.color.toEntityColor()
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