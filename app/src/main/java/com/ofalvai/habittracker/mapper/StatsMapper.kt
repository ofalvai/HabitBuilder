package com.ofalvai.habittracker.mapper

import com.kizitonwose.calendarview.utils.yearMonth
import com.ofalvai.habittracker.persistence.entity.ActionCompletionRate
import com.ofalvai.habittracker.persistence.entity.SumActionCountByDay
import com.ofalvai.habittracker.ui.model.ActionCountByMonth
import com.ofalvai.habittracker.ui.model.ActionCountByWeek
import com.ofalvai.habittracker.ui.model.GeneralHabitStats
import com.ofalvai.habittracker.ui.model.HeatmapMonth
import java.time.*
import com.ofalvai.habittracker.persistence.entity.ActionCountByMonth as ActionCountByMonthEntity
import com.ofalvai.habittracker.persistence.entity.ActionCountByWeek as ActionCountByWeekEntity

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

fun mapSumActionCountByDay(
    entityList: List<SumActionCountByDay>,
    yearMonth: YearMonth,
    totalHabitCount: Int
): HeatmapMonth {
    val dayMap = mutableMapOf<LocalDate, HeatmapMonth.BucketInfo>()

    entityList.forEach {
        if (it.date.yearMonth == yearMonth) {
            dayMap[it.date] = actionCountToBucket(totalHabitCount, it.action_count)
        }
    }

    return HeatmapMonth(yearMonth, dayMap, totalHabitCount)
}

private fun actionCountToBucket(totalHabitCount: Int, actionCount: Int): HeatmapMonth.BucketInfo {
    if (totalHabitCount == 0 || actionCount == 0) {
        return HeatmapMonth.BucketInfo(bucketIndex = 0, value = 0)
    }

    val ratio = actionCount / totalHabitCount.toFloat()

    val bucketIndex = when {
        (0f..0.25f).contains(ratio) -> 1
        (0.25f..0.5f).contains(ratio) -> 2
        (0.5f..0.75f).contains(ratio) -> 3
        else -> 4
    }

    return HeatmapMonth.BucketInfo(bucketIndex = bucketIndex, value = actionCount)
}