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

package com.ofalvai.habittracker.feature.insights.mapper

import com.ofalvai.habittracker.core.database.entity.HabitActionCount
import com.ofalvai.habittracker.core.database.entity.HabitTopDay
import com.ofalvai.habittracker.core.database.entity.SumActionCountByDay
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.feature.insights.model.BucketIndex
import com.ofalvai.habittracker.feature.insights.model.HeatmapMonth
import com.ofalvai.habittracker.feature.insights.model.TopDayItem
import com.ofalvai.habittracker.feature.insights.model.TopHabitItem
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.min
import com.ofalvai.habittracker.core.database.entity.Habit as HabitEntity

private const val maxBucketCount = 5

fun mapSumActionCountByDay(
    entityList: List<SumActionCountByDay>,
    yearMonth: YearMonth,
    totalHabitCount: Int
): HeatmapMonth {
    val dayMap = mutableMapOf<LocalDate, HeatmapMonth.BucketInfo>()

    entityList.forEach {
        it.date?.let { date ->
            if (YearMonth.of(date.year, date.month) == yearMonth) {
                dayMap[it.date!!] = actionCountToBucket(totalHabitCount, it.action_count)
            }
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

fun HabitEntity.toModel() = Habit(
    id = id,
    name = name,
    color = color.toModelColor(),
    notes = notes
)

fun HabitEntity.Color.toModelColor(): Habit.Color = when (this) {
    HabitEntity.Color.Red -> Habit.Color.Red
    HabitEntity.Color.Green -> Habit.Color.Green
    HabitEntity.Color.Blue -> Habit.Color.Blue
    HabitEntity.Color.Yellow -> Habit.Color.Yellow
    HabitEntity.Color.Cyan -> Habit.Color.Cyan
    HabitEntity.Color.Pink -> Habit.Color.Pink
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