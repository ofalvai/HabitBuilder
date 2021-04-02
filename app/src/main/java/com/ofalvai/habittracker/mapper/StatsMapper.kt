package com.ofalvai.habittracker.mapper

import com.ofalvai.habittracker.persistence.entity.ActionCompletionRate
import com.ofalvai.habittracker.ui.model.ActionCountByMonth
import com.ofalvai.habittracker.persistence.entity.ActionCountByMonth as ActionCountByMonthEntity
import com.ofalvai.habittracker.ui.model.ActionCountByWeek
import com.ofalvai.habittracker.persistence.entity.ActionCountByWeek as ActionCountByWeekEntity
import com.ofalvai.habittracker.ui.model.GeneralHabitStats
import java.time.*

fun mapHabitStatsToModel(completionRate: ActionCompletionRate): GeneralHabitStats {
    val firstDayDate = LocalDateTime.ofInstant(
        completionRate.first_day, ZoneId.systemDefault()
    ).toLocalDate()

    return GeneralHabitStats(
        firstDay = if (completionRate.first_day == Instant.EPOCH) null else firstDayDate,
        actionCount = completionRate.action_count,
        completionRate = completionRate.rateAsOf(LocalDate.now())
    )
}

fun mapActionCountByWeek(entityList: List<ActionCountByWeekEntity>): List<ActionCountByWeek> {
    return entityList.map {
        ActionCountByWeek(
            year = Year.of(it.year),
            weekOfYear = it.week,
            actionCount = it.action_count
        )
    }
}

fun mapActionCountByMonth(entityList: List<ActionCountByMonthEntity>): List<ActionCountByMonth> {
    return entityList.map {
        ActionCountByMonth(
            yearMonth = YearMonth.of(it.year, it.month),
            actionCount = it.action_count
        )
    }
}