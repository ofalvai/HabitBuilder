package com.ofalvai.habittracker

import com.ofalvai.habittracker.mapper.mapSumActionCountByDay
import com.ofalvai.habittracker.persistence.entity.SumActionCountByDay
import com.ofalvai.habittracker.ui.model.HeatmapMonth
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

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
            totalHabitCount = 0
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
            totalHabitCount = 4
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
            totalHabitCount = 4
        )
        assertEquals(expected, heatmap)
    }
}