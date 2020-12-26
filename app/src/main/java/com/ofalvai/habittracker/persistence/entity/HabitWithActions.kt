package com.ofalvai.habittracker.persistence.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HabitWithActions(
    @Embedded val habit: Habit,

    @Relation(
        parentColumn = "id",
        entityColumn = "habit_id"
    )
    val actions: List<Action>
)