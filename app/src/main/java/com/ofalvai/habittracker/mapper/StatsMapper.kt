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

import com.kizitonwose.calendarview.utils.yearMonth
import com.ofalvai.habittracker.core.database.entity.ActionCompletionRate
import com.ofalvai.habittracker.core.database.entity.HabitActionCount
import com.ofalvai.habittracker.core.database.entity.HabitTopDay
import com.ofalvai.habittracker.core.database.entity.SumActionCountByDay
import com.ofalvai.habittracker.ui.model.*
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import java.time.*
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.min
import com.ofalvai.habittracker.core.database.entity.ActionCountByMonth as ActionCountByMonthEntity
import com.ofalvai.habittracker.core.database.entity.ActionCountByWeek as ActionCountByWeekEntity

fun mapHabitSingleStats(
    completionRate: ActionCompletionRate,
    actionCountByWeekEntity: List<ActionCountByWeekEntity>,
    now: LocalDate,
    locale: Locale
): SingleStats {
    val firstDayDate = LocalDateTime.ofInstant(
        completionRate.first_day, ZoneId.systemDefault()
    ).toLocalDate()
    val lastWeekActions = actionCountByWeekEntity.lastOrNull {
        val weekFields = WeekFields.of(locale)
        it.year == now.year && it.week == now.get(weekFields.weekOfWeekBasedYear())
    }

    return SingleStats(
        firstDay = if (completionRate.first_day == Instant.EPOCH) null else firstDayDate,
        actionCount = completionRate.action_count,
        weeklyActionCount = lastWeekActions?.action_count ?: 0,
        completionRate = completionRate.rateAsOf(now)
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

private const val maxBucketCount = 5

fun mapSumActionCountByDay(
    entityList: List<SumActionCountByDay>,
    yearMonth: YearMonth,
    totalHabitCount: Int
): HeatmapMonth {
    val dayMap = mutableMapOf<LocalDate, HeatmapMonth.BucketInfo>()

    entityList.forEach {
        if (it.date?.yearMonth == yearMonth) {
            dayMap[it.date!!] = actionCountToBucket(totalHabitCount, it.action_count)
        }
    }

    val bucketData = habitCountToBucketMaxValues(totalHabitCount)

    return HeatmapMonth(
        yearMonth,
        dayMap.toImmutableMap(),
        totalHabitCount,
        bucketCount = bucketData.size,
        bucketMaxValues = bucketData.toImmutableList()
    )
}

fun mapHabitActionCount(entity: HabitActionCount, now: LocalDate): TopHabitItem {
    val activeDays = ChronoUnit.DAYS.between(entity.first_day, now)
    val progress = if (activeDays > 0) entity.count / activeDays.toFloat() else 0f

    return TopHabitItem(
        habitId = entity.habit_id,
        name = entity.name,
        count = entity.count,
        progress = progress
    )
}

fun mapHabitTopDay(entity: HabitTopDay, locale: Locale): TopDayItem {
    return TopDayItem(
        habitId = entity.habit_id,
        name = entity.name,
        dayLabel = if (entity.action_count_on_day == 0) "–" else {
            entity.top_day_of_week.getDisplayName(TextStyle.FULL, locale)
        },
        count = entity.action_count_on_day
    )
}

private fun actionCountToBucket(totalHabitCount: Int, actionCount: Int): HeatmapMonth.BucketInfo {
    if (totalHabitCount == 0 || actionCount == 0) {
        return HeatmapMonth.BucketInfo(bucketIndex = 0, value = 0)
    }

    if (totalHabitCount < maxBucketCount) {
        // If habit count < 5 we use 1 bucket for each action count value (+1 for the value 0)
        return HeatmapMonth.BucketInfo(
            bucketIndex = min(actionCount, totalHabitCount), // This should never happen
            value = actionCount
        )
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

private fun habitCountToBucketMaxValues(totalHabitCount: Int): List<Pair<BucketIndex, Int>> {
    if (totalHabitCount == 0) {
        return emptyList()
    }

    if (totalHabitCount < maxBucketCount) {
        // If habit count < 5 we use 1 bucket for each action count value + 1 bucket for 0
        return (0..totalHabitCount).map {
            if (it == 0) {
                Pair(0, 0) // First bucket is always for the value 0
            } else {
                Pair(it, it)
            }
        }
    } else {
        // If habit count >= 5 we use 5 buckets and group values to find the largest in each bucket
        val bucketMap = mutableMapOf<BucketIndex, MutableSet<Int>>()
        (0.until(maxBucketCount)).forEach {
            // Initialize empty sets for every bucket index
            bucketMap[it] = mutableSetOf()
        }
        (0..totalHabitCount).forEach {
            // Put each possible action count value into a bucket
            val bucketInfo = actionCountToBucket(totalHabitCount, it)
            bucketMap[bucketInfo.bucketIndex]!!.add(bucketInfo.value)
        }

        return bucketMap
            .map { entry -> Pair(entry.key, entry.value.maxOrNull() ?: 0) }
            .sortedBy { it.first } // Sort by bucket index
    }
}