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

package com.ofalvai.habittracker

import com.ofalvai.habittracker.core.database.entity.*
import com.ofalvai.habittracker.mapper.mapHabitActionCount
import com.ofalvai.habittracker.mapper.mapHabitSingleStats
import com.ofalvai.habittracker.mapper.mapHabitTopDay
import com.ofalvai.habittracker.mapper.mapSumActionCountByDay
import com.ofalvai.habittracker.ui.model.HeatmapMonth
import com.ofalvai.habittracker.ui.model.SingleStats
import com.ofalvai.habittracker.ui.model.TopDayItem
import com.ofalvai.habittracker.ui.model.TopHabitItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.*
import java.util.*

class StatsMapperTest {

    @Test
    fun `Given no habits and actions When mapped to heatmap Then map contains no data`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 4)
        val totalHabitCount = 0

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 0,
            bucketCount = 0,
            bucketMaxValues = emptyList()
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given habits and no actions When mapped to heatmap Then bucketing is correct`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 4)
        val totalHabitCount = 2

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 2,
            bucketCount = 3,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1,
                2 to 2
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given habits and actions in the target month When mapped to heatmap Then bucketing is correct`() {
        // Given
        val actionCountEntityList = listOf(
            SumActionCountByDay(LocalDate.of(2021, 4, 20), 2),
            SumActionCountByDay(LocalDate.of(2021, 4, 21), 1),
            SumActionCountByDay(LocalDate.of(2021, 4, 22), 0),
            SumActionCountByDay(LocalDate.of(2021, 4, 23), 3),
            SumActionCountByDay(LocalDate.of(2021, 4, 24), 4)
        )
        val yearMonth = YearMonth.of(2021, 4)
        val totalHabitCount = 4

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = mapOf(
                LocalDate.of(2021, 4, 20) to HeatmapMonth.BucketInfo(2, 2),
                LocalDate.of(2021, 4, 21) to HeatmapMonth.BucketInfo(1, 1),
                LocalDate.of(2021, 4, 22) to HeatmapMonth.BucketInfo(0, 0),
                LocalDate.of(2021, 4, 23) to HeatmapMonth.BucketInfo(3, 3),
                LocalDate.of(2021, 4, 24) to HeatmapMonth.BucketInfo(4, 4),
            ),
            totalHabitCount = 4,
            bucketCount = 5,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1,
                2 to 2,
                3 to 3,
                4 to 4
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given actions outside the target month When mapped to heatmap Then these are filtered`() {
        // Given
        val actionCountEntityList = listOf(
            SumActionCountByDay(LocalDate.of(2021, 3, 15), 4),
            SumActionCountByDay(LocalDate.of(2021, 4, 20), 2),
            SumActionCountByDay(LocalDate.of(2021, 4, 21), 1),
            SumActionCountByDay(LocalDate.of(2021, 4, 22), 0),
            SumActionCountByDay(LocalDate.of(2021, 4, 23), 3),
            SumActionCountByDay(LocalDate.of(2021, 4, 24), 4),
            SumActionCountByDay(LocalDate.of(2020, 3, 20), 2),
        )
        val yearMonth = YearMonth.of(2021, 3)
        val totalHabitCount = 4

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = mapOf(
                LocalDate.of(2021, 3, 15) to HeatmapMonth.BucketInfo(4, 4)
            ),
            totalHabitCount = 4,
            bucketCount = 5,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1,
                2 to 2,
                3 to 3,
                4 to 4
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given 1 habit When mapped to heatmap Then legend has 2 buckets with actual action count values`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 5)
        val totalHabitCount = 1

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 1,
            bucketCount = 2,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given 2 habits When mapped to heatmap Then legend has 3 buckets with actual action count values`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 5)
        val totalHabitCount = 2

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 2,
            bucketCount = 3,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1,
                2 to 2
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given 3 habits When mapped to heatmap Then legend has 4 buckets with actual action count values`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 5)
        val totalHabitCount = 3

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 3,
            bucketCount = 4,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1,
                2 to 2,
                3 to 3
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given 4 habits When mapped to heatmap Then legend has 5 buckets with actual action count values`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 5)
        val totalHabitCount = 4

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 4,
            bucketCount = 5,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1,
                2 to 2,
                3 to 3,
                4 to 4
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given 5 habits When mapped to heatmap Then legend has 5 buckets with the highest value in each bucket`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 5)
        val totalHabitCount = 5

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 5,
            bucketCount = 5,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1,
                2 to 2,
                3 to 3,
                4 to 5
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given 6 habits When mapped to heatmap Then legend has 5 buckets with the highest value in each bucket`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 5)
        val totalHabitCount = 6

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 6,
            bucketCount = 5,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1,
                2 to 3,
                3 to 4,
                4 to 6
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given 7 habits When mapped to heatmap Then legend has 5 buckets with the highest value in each bucket`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 5)
        val totalHabitCount = 7

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 7,
            bucketCount = 5,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 1,
                2 to 3,
                3 to 5,
                4 to 7
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given 8 habits When mapped to heatmap Then legend has 5 buckets with the highest value in each bucket`() {
        // Given
        val actionCountEntityList = emptyList<SumActionCountByDay>()
        val yearMonth = YearMonth.of(2021, 5)
        val totalHabitCount = 8

        // When
        val heatmap = mapSumActionCountByDay(actionCountEntityList, yearMonth, totalHabitCount)

        // Then
        val expected = HeatmapMonth(
            yearMonth = yearMonth,
            dayMap = emptyMap(),
            totalHabitCount = 8,
            bucketCount = 5,
            bucketMaxValues = listOf(
                0 to 0,
                1 to 2,
                2 to 4,
                3 to 6,
                4 to 8
            )
        )
        assertEquals(expected, heatmap)
    }

    @Test
    fun `Given habit with first action today When mapped to top habit item Then progress and count are 0`() {
        // Given
        val now = LocalDate.of(2021, 5, 23)
        val habitActionCount = HabitActionCount(
            habit_id = 1,
            name = "New habit",
            first_day = now,
            count = 1
        )

        // When
        val topHabitItem = mapHabitActionCount(habitActionCount, now)

        // Then
        val expected = TopHabitItem(
            habitId = 1,
            name = "New habit",
            count = 1,
            progress = 0f
        )
        assertEquals(expected, topHabitItem)
    }

    @Test
    fun `Given habit with many actions in the past When mapped to top habit item Then progress and count are correct`() {
        // Given
        val now = LocalDate.of(2021, 5, 23)
        val habitActionCount = HabitActionCount(
            habit_id = 1,
            name = "Old habit",
            first_day = LocalDate.of(2021, 2, 14),
            count = 16
        )

        // When
        val topHabitItem = mapHabitActionCount(habitActionCount, now)

        // Then
        val expected = TopHabitItem(
            habitId = 1,
            name = "Old habit",
            count = 16,
            progress = 0.1632653f
        )
        assertEquals(expected, topHabitItem)
    }

    @Test
    fun `Given habit with no actions When single stats are mapped Then stats are empty`() {
        // Given
        val now = LocalDate.now()
        val locale = Locale("hu", "hu")
        val completionRate = ActionCompletionRate(
            first_day = Instant.EPOCH,
            action_count = 0
        )
        val weeklyActionCountList = emptyList<ActionCountByWeek>()

        // When
        val singleStats = mapHabitSingleStats(completionRate, weeklyActionCountList, now, locale)

        // Then
        val expected = SingleStats(
            firstDay = null,
            actionCount = 0,
            weeklyActionCount = 0,
            completionRate = 0f
        )
        assertEquals(expected, singleStats)
    }

    @Test
    fun `Given habit with single action today When single stats are mapped Then stats are correct`() {
        // Given
        val now = LocalDate.of(2021, 5, 24)
        val locale = Locale("hu", "hu")
        val completionRate = ActionCompletionRate(
            first_day = now.atTime(9, 24).toInstant(OffsetDateTime.now().offset),
            action_count = 1
        )
        val weeklyActionCountList = listOf(
            ActionCountByWeek(
                year = 2021,
                week = 21,
                1
            )
        )

        // When
        val singleStats = mapHabitSingleStats(completionRate, weeklyActionCountList, now, locale)

        // Then
        val expected = SingleStats(
            firstDay = LocalDate.of(2021, 5, 24),
            actionCount = 1,
            weeklyActionCount = 1,
            completionRate = 1f
        )
        assertEquals(expected, singleStats)
    }

    @Test
    fun `Given habit with actions last week and this week When single stats are mapped Then stats are correct`() {
        // Given
        val now = LocalDate.of(2021, 5, 24)
        val locale = Locale("hu", "hu")
        val completionRate = ActionCompletionRate(
            first_day = LocalDateTime
                .of(2021, 5, 18, 9, 24)
                .toInstant(OffsetDateTime.now().offset),
            action_count = 5
        )
        val weeklyActionCountList = listOf(
            ActionCountByWeek(
                year = 2021,
                week = 20,
                action_count = 4
            ),
            ActionCountByWeek(
                year = 2021,
                week = 21,
                action_count = 1
            )
        )

        // When
        val singleStats = mapHabitSingleStats(completionRate, weeklyActionCountList, now, locale)

        // Then
        val expected = SingleStats(
            firstDay = LocalDate.of(2021, 5, 18),
            actionCount = 5,
            weeklyActionCount = 1,
            completionRate = 0.7142857143f
        )
        assertEquals(expected, singleStats)
    }

    @Test
    fun `Given habit with actions last week When single stats are mapped Then stats are correct`() {
        // Given
        val now = LocalDate.of(2021, 5, 24)
        val locale = Locale("hu", "hu")
        val completionRate = ActionCompletionRate(
            first_day = LocalDateTime
                .of(2021, 5, 18, 9, 24)
                .toInstant(OffsetDateTime.now().offset),
            action_count = 4
        )
        val weeklyActionCountList = listOf(
            ActionCountByWeek(
                year = 2021,
                week = 20,
                action_count = 4
            )
        )

        // When
        val singleStats = mapHabitSingleStats(completionRate, weeklyActionCountList, now, locale)

        // Then
        val expected = SingleStats(
            firstDay = LocalDate.of(2021, 5, 18),
            actionCount = 4,
            weeklyActionCount = 0,
            completionRate = 0.5714285714f
        )
        assertEquals(expected, singleStats)
    }

    @Test
    fun `Given habit top days and locale When mapped Then weekday representation is correct`() {
        // Given
        val locale = Locale.US
        val topDays = listOf(
            HabitTopDay(0, "Sunday habit", DayOfWeek.SUNDAY, 2),
            HabitTopDay(0, "Monday habit", DayOfWeek.MONDAY, 13),
            HabitTopDay(0, "Tuesday habit", DayOfWeek.TUESDAY, 3),
            HabitTopDay(
                0,
                "Wednesday habit",
                DayOfWeek.WEDNESDAY,
                234
            ),
            HabitTopDay(
                0,
                "Thursday habit",
                DayOfWeek.THURSDAY,
                2
            ),
            HabitTopDay(0, "Friday habit", DayOfWeek.FRIDAY, 6),
            HabitTopDay(
                0,
                "Saturday habit",
                DayOfWeek.SATURDAY,
                1
            ),
            HabitTopDay(0, "Empty habit", DayOfWeek.SUNDAY, 0),
        )

        // When + then
        assertEquals(
            TopDayItem(0, "Sunday habit", "Sunday", 2),
            mapHabitTopDay(topDays[0], locale)
        )
        assertEquals(
            TopDayItem(0, "Monday habit", "Monday", 13),
            mapHabitTopDay(topDays[1], locale)
        )
        assertEquals(
            TopDayItem(0, "Tuesday habit", "Tuesday", 3),
            mapHabitTopDay(topDays[2], locale)
        )
        assertEquals(
            TopDayItem(0, "Wednesday habit", "Wednesday", 234),
            mapHabitTopDay(topDays[3], locale)
        )
        assertEquals(
            TopDayItem(0, "Thursday habit", "Thursday", 2),
            mapHabitTopDay(topDays[4], locale)
        )
        assertEquals(
            TopDayItem(0, "Friday habit", "Friday", 6),
            mapHabitTopDay(topDays[5], locale)
        )
        assertEquals(
            TopDayItem(0, "Saturday habit", "Saturday", 1),
            mapHabitTopDay(topDays[6], locale)
        )
        assertEquals(
            TopDayItem(0, "Empty habit", "–", 0),
            mapHabitTopDay(topDays[7], locale)
        )
    }
}