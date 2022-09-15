/*
 * Copyright 2022 Olivér Falvai
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

import com.kizitonwose.calendarview.utils.yearMonth
import com.ofalvai.habittracker.ui.model.ActionCountByMonth
import com.ofalvai.habittracker.ui.model.ActionCountByWeek
import com.ofalvai.habittracker.ui.model.ActionCountChart
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.*
import java.time.temporal.IsoFields
import java.time.temporal.WeekFields
import java.util.*
import com.ofalvai.habittracker.core.database.entity.ActionCountByMonth as ActionCountByMonthEntity
import com.ofalvai.habittracker.core.database.entity.ActionCountByWeek as ActionCountByWeekEntity

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

fun mapActionCountByMonthListToItemList(
    list: List<ActionCountByMonth>, today: LocalDate
): ImmutableList<ActionCountChart.ChartItem> {
    return fillActionCountByMonthListWithEmptyItems(list, today)
        .map { it.toChartItem() }.toImmutableList()
}

fun mapActionCountByWeekListToItemList(
    list: List<ActionCountByWeek>,
    today: LocalDate,
    locale: Locale
): ImmutableList<ActionCountChart.ChartItem> {
    return fillActionCountByWeekListWithEmptyItems(list, today, locale)
        .map { it.toChartItem() }.toImmutableList()
}

fun fillActionCountByMonthListWithEmptyItems(
    list: List<ActionCountByMonth>, today: LocalDate
): List<ActionCountByMonth> {
    // Add today's month to the end of list, even if it's missing
    val listWithCurrentMonth = if (list.lastOrNull()?.yearMonth == today.yearMonth) {
        list
    } else {
        list + listOf(ActionCountByMonth(today.yearMonth, 0))
    }

    val filledList = mutableListOf<ActionCountByMonth>()
    var previousYearMonth: YearMonth? = null
    for (item in listWithCurrentMonth) {
        if (previousYearMonth == null) {
            previousYearMonth = item.yearMonth
            filledList.add(item)
            continue
        }

        val skippedMonths = Period.between(
            previousYearMonth.atDay(1),
            item.yearMonth.atDay(1)
        ).months - 1

        repeat(skippedMonths) { index ->
            val newMonth = previousYearMonth!!.plusMonths((1 + index).toLong())
            filledList.add(ActionCountByMonth(newMonth, 0))
        }
        previousYearMonth = item.yearMonth
        filledList.add(item)
    }

    return filledList
}

fun fillActionCountByWeekListWithEmptyItems(
    list: List<ActionCountByWeek>,
    today: LocalDate,
    locale: Locale
): List<ActionCountByWeek> {
    // Add today's week to the end of list, even if it's missing
    val last = list.lastOrNull()
    val todayWeekOfYear = today.get(WeekFields.of(locale).weekOfWeekBasedYear())
    val listWithCurrentWeek = if (
        last?.year?.value == today.year && last.weekOfYear == todayWeekOfYear
    ) {
        list
    } else {
        list + listOf(ActionCountByWeek(Year.of(today.year), todayWeekOfYear, 0))
    }


    val filledList = mutableListOf<ActionCountByWeek>()

    var previousYear: Year? = null
    var previousWeek: Int? = null
    for (item in listWithCurrentWeek) {
        if (previousWeek == null && previousYear == null) {
            previousYear = item.year
            previousWeek = item.weekOfYear
            filledList.add(item)
            continue
        }

        val lastDayOfPreviousYear = previousYear!!.atMonthDay(MonthDay.of(12, 31))
        val weeksInPreviousYear = IsoFields.WEEK_OF_WEEK_BASED_YEAR
            .rangeRefinedBy(lastDayOfPreviousYear).maximum.toInt()

        val skippedWeeks = if (item.weekOfYear >= previousWeek!!) {
            // Both weeks are in the same year
            item.weekOfYear - previousWeek - 1
        } else {
            // Some skipped weeks in last year + some in current year
            weeksInPreviousYear - previousWeek + item.weekOfYear - 1
        }

        repeat(skippedWeeks) { index ->
            val newWeek = previousWeek!! + 1 + index
            val fillItem = if (newWeek > weeksInPreviousYear) {
                // New week is in next year
                ActionCountByWeek(
                    year = previousYear!!.plusYears(1),
                    weekOfYear = newWeek % weeksInPreviousYear,
                    actionCount = 0
                )
            } else {
                // New week is in the same year
                ActionCountByWeek(year = previousYear!!, weekOfYear = newWeek, actionCount = 0)
            }

            filledList.add(fillItem)
        }
        previousYear = item.year
        previousWeek = item.weekOfYear
        filledList.add(item)
    }

    return filledList
}


private fun ActionCountByWeek.toChartItem() = ActionCountChart.ChartItem(
    label = "W${this.weekOfYear}",
    year = this.year.value,
    value = this.actionCount
)

private fun ActionCountByMonth.toChartItem() = ActionCountChart.ChartItem(
    label = this.yearMonth.monthValue.toString(),
    year = this.yearMonth.year,
    value = this.actionCount,
)