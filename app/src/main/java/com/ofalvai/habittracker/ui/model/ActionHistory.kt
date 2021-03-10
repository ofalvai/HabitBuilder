package com.ofalvai.habittracker.ui.model

sealed class ActionHistory {

    object Clean : ActionHistory()

    data class Streak(val days: Int) : ActionHistory()

    data class MissedDays(val days: Int) : ActionHistory()
}
