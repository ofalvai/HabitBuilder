package com.ofalvai.habittracker.ui.model

data class HabitWithActions(
    val habit: Habit,
    val actions: List<Action>
)