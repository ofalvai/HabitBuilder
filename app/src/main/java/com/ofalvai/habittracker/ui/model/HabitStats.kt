package com.ofalvai.habittracker.ui.model

import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

data class GeneralHabitStats(
    val firstDay: LocalDate?,
    val actionCount: Int,
    val completionRate: Float // TODO: move computation from entity to mapper
)

data class ActionCountByWeek(
    val year: Year,
    val weekOfYear: Int,
    val actionCount: Int
)

data class ActionCountByMonth(
    val yearMonth: YearMonth,
    val actionCount: Int
)