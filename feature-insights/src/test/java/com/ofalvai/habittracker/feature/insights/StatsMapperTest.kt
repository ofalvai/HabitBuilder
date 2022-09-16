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

package com.ofalvai.habittracker.feature.insights

import com.ofalvai.habittracker.core.database.entity.HabitActionCount
import com.ofalvai.habittracker.core.database.entity.HabitTopDay
import com.ofalvai.habittracker.core.database.entity.SumActionCountByDay
import com.ofalvai.habittracker.feature.insights.mapper.mapHabitActionCount
import com.ofalvai.habittracker.feature.insights.mapper.mapHabitTopDay
import com.ofalvai.habittracker.feature.insights.mapper.mapSumActionCountByDay
import com.ofalvai.habittracker.feature.insights.model.HeatmapMonth
import com.ofalvai.habittracker.feature.insights.model.TopDayItem
import com.ofalvai.habittracker.feature.insights.model.TopHabitItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
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
            dayMap = persistentMapOf(),
            totalHabitCount = 0,
            bucketCount = 0,
            bucketMaxValues = persistentListOf()
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
            dayMap = persistentMapOf(),
            totalHabitCount = 2,
            bucketCount = 3,
            bucketMaxValues =persistentListOf(
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
        val actionCountEntityList = persistentListOf(
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
            dayMap = persistentMapOf(
                LocalDate.of(2021, 4, 20) to HeatmapMonth.BucketInfo(2, 2),
                LocalDate.of(2021, 4, 21) to HeatmapMonth.BucketInfo(1, 1),
                LocalDate.of(2021, 4, 22) to HeatmapMonth.BucketInfo(0, 0),
                LocalDate.of(2021, 4, 23) to HeatmapMonth.BucketInfo(3, 3),
                LocalDate.of(2021, 4, 24) to HeatmapMonth.BucketInfo(4, 4),
            ),
            totalHabitCount = 4,
            bucketCount = 5,
            bucketMaxValues = persistentListOf(
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
        val actionCountEntityList = persistentListOf(
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
            dayMap = persistentMapOf(
                LocalDate.of(2021, 3, 15) to HeatmapMonth.BucketInfo(4, 4)
            ),
            totalHabitCount = 4,
            bucketCount = 5,
            bucketMaxValues = persistentListOf(
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
            dayMap = persistentMapOf(),
            totalHabitCount = 1,
            bucketCount = 2,
            bucketMaxValues = persistentListOf(
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
            dayMap = persistentMapOf(),
            totalHabitCount = 2,
            bucketCount = 3,
            bucketMaxValues = persistentListOf(
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
            dayMap = persistentMapOf(),
            totalHabitCount = 3,
            bucketCount = 4,
            bucketMaxValues = persistentListOf(
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
            dayMap = persistentMapOf(),
            totalHabitCount = 4,
            bucketCount = 5,
            bucketMaxValues = persistentListOf(
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
            dayMap = persistentMapOf(),
            totalHabitCount = 5,
            bucketCount = 5,
            bucketMaxValues = persistentListOf(
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
            dayMap = persistentMapOf(),
            totalHabitCount = 6,
            bucketCount = 5,
            bucketMaxValues = persistentListOf(
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
            dayMap = persistentMapOf(),
            totalHabitCount = 7,
            bucketCount = 5,
            bucketMaxValues = persistentListOf(
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
            dayMap = persistentMapOf(),
            totalHabitCount = 8,
            bucketCount = 5,
            bucketMaxValues = persistentListOf(
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
    fun `Given habit top days and locale When mapped Then weekday representation is correct`() {
        // Given
        val locale = Locale.US
        val topDays = persistentListOf(
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