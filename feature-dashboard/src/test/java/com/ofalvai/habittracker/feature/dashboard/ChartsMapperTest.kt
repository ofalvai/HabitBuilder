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

package com.ofalvai.habittracker.feature.dashboard

import com.ofalvai.habittracker.feature.dashboard.mapper.mapActionCountByMonthListToItemList
import com.ofalvai.habittracker.feature.dashboard.mapper.mapActionCountByWeekListToItemList
import com.ofalvai.habittracker.feature.dashboard.ui.model.ActionCountByMonth
import com.ofalvai.habittracker.feature.dashboard.ui.model.ActionCountByWeek
import com.ofalvai.habittracker.feature.dashboard.ui.model.ActionCountChart.ChartItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.util.*

class ChartsMapperTest {

    private val locale = Locale.forLanguageTag("hu-HU")

    @Test
    fun `Given consecutive months When mapped Then no filling item is added`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2021, 8), 1),
            ActionCountByMonth(YearMonth.of(2021, 9), 6),
            ActionCountByMonth(YearMonth.of(2021, 10), 12)
        )
        val today = LocalDate.of(2021, 10, 16)

        // When
        val result = mapActionCountByMonthListToItemList(list, today)

        // Then
        val expected = listOf(
            ChartItem("8", 2021, 1),
            ChartItem("9", 2021, 6),
            ChartItem("10", 2021, 12)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given empty month list When mapped Then result is only the current month`() {
        // Given
        val list = emptyList<ActionCountByMonth>()
        val today = LocalDate.of(2021, 10, 16)

        // When
        val result = mapActionCountByMonthListToItemList(list, today)

        // Then
        val expected = listOf(ChartItem("10", 2021, 0))
        assertEquals(expected, result)
    }

    @Test
    fun `Given single month When mapped Then result is single month too`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2021, 10), 12)
        )
        val today = LocalDate.of(2021, 10, 16)

        // When
        val result = mapActionCountByMonthListToItemList(list, today)

        // Then
        val expected = listOf(
            ChartItem("10", 2021, 12)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given a single gap of 1 month and empty current month When mapped Then two item is added`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2021, 8), 1),
            ActionCountByMonth(YearMonth.of(2021, 10), 12)
        )
        val today = LocalDate.of(2021, 11, 16)

        // When
        val result = mapActionCountByMonthListToItemList(list, today)

        // Then
        val expected = listOf(
            ChartItem("8", 2021, 1),
            ChartItem("9", 2021, 0),
            ChartItem("10", 2021, 12),
            ChartItem("11", 2021, 0)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given a single gap of multiple months When mapped Then multiple items are added`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2021, 4), 1),
            ActionCountByMonth(YearMonth.of(2021, 10), 12)
        )
        val today = LocalDate.of(2021, 11, 16)

        // When
        val result = mapActionCountByMonthListToItemList(list, today)

        // Then
        val expected = listOf(
            ChartItem("4", 2021, 1),
            ChartItem("5", 2021, 0),
            ChartItem("6", 2021, 0),
            ChartItem("7", 2021, 0),
            ChartItem("8", 2021, 0),
            ChartItem("9", 2021, 0),
            ChartItem("10", 2021, 12),
            ChartItem("11", 2021, 0)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given multiple gaps of 1 month When mapped Then multiple items are added`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2021, 8), 1),
            ActionCountByMonth(YearMonth.of(2021, 10), 12),
            ActionCountByMonth(YearMonth.of(2021, 12), 24),
        )
        val today = LocalDate.of(2021, 12, 16)

        // When
        val result = mapActionCountByMonthListToItemList(list, today)

        // Then
        val expected = listOf(
            ChartItem("8", 2021, 1),
            ChartItem("9", 2021, 0),
            ChartItem("10", 2021, 12),
            ChartItem("11", 2021, 0),
            ChartItem("12", 2021, 24),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given multiple gaps of multiple months When mapped Then multiple items are added`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2021, 5), 1),
            ActionCountByMonth(YearMonth.of(2021, 9), 12),
            ActionCountByMonth(YearMonth.of(2021, 12), 24),
        )
        val today = LocalDate.of(2021, 12, 16)

        // When
        val result = mapActionCountByMonthListToItemList(list, today)

        // Then
        val expected = listOf(
            ChartItem("5", 2021, 1),
            ChartItem("6", 2021, 0),
            ChartItem("7", 2021, 0),
            ChartItem("8", 2021, 0),
            ChartItem("9", 2021, 12),
            ChartItem("10", 2021, 0),
            ChartItem("11", 2021, 0),
            ChartItem("12", 2021, 24),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given multiple gaps spanning across years and empty current month When mapped Then correct number of items are added`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2020, 11), 11),
            ActionCountByMonth(YearMonth.of(2021, 3), 3),
            ActionCountByMonth(YearMonth.of(2021, 10), 10),
            ActionCountByMonth(YearMonth.of(2021, 12), 12),
            ActionCountByMonth(YearMonth.of(2022, 2), 2),
        )
        val today = LocalDate.of(2022, 3, 16)

        // When
        val result = mapActionCountByMonthListToItemList(list, today)

        // Then
        val expected = listOf(
            ChartItem("11", 2020, 11),
            ChartItem("12", 2020, 0),
            ChartItem("1", 2021, 0),
            ChartItem("2", 2021, 0),
            ChartItem("3", 2021, 3),
            ChartItem("4", 2021, 0),
            ChartItem("5", 2021, 0),
            ChartItem("6", 2021, 0),
            ChartItem("7", 2021, 0),
            ChartItem("8", 2021, 0),
            ChartItem("9", 2021, 0),
            ChartItem("10", 2021, 10),
            ChartItem("11", 2021, 0),
            ChartItem("12", 2021, 12),
            ChartItem("1", 2022, 0),
            ChartItem("2", 2022, 2),
            ChartItem("3", 2022, 0),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given consecutive weeks When mapped Then no filling item is added`() {
        // Given
        val list = listOf(
            ActionCountByWeek(Year.of(2021), 8, 1),
            ActionCountByWeek(Year.of(2021), 9, 6),
            ActionCountByWeek(Year.of(2021), 10, 12)
        )
        val today = LocalDate.of(2021, 3, 10)

        // When
        val result = mapActionCountByWeekListToItemList(list, today, locale)

        // Then
        val expected = listOf(
            ChartItem("W8", 2021, 1),
            ChartItem("W9", 2021, 6),
            ChartItem("W10", 2021, 12)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given empty week list When mapped Then result is the current week only`() {
        // Given
        val list = emptyList<ActionCountByWeek>()
        val today = LocalDate.of(2021, 10, 16)

        // When
        val result = mapActionCountByWeekListToItemList(list, today, locale)

        // Then
        val expected = listOf(ChartItem("W41", 2021, 0))
        assertEquals(expected, result)
    }

    @Test
    fun `Given single week When mapped Then result is single item`() {
        // Given
        val list = listOf(ActionCountByWeek(Year.of(2021), 10, 16))
        val today = LocalDate.of(2021, 3, 10)

        // When
        val result = mapActionCountByWeekListToItemList(list, today, locale)

        // Then
        val expected = listOf(ChartItem("W10", 2021, 16))
        assertEquals(expected, result)
    }

    @Test
    fun `Given a single gap of 1 week and empty current week When mapped Then 2 items are added`() {
        // Given
        val list = listOf(
            ActionCountByWeek(Year.of(2021), 8, 1),
            ActionCountByWeek(Year.of(2021), 10, 12)
        )
        val today = LocalDate.of(2021, 3, 16)

        // When
        val result = mapActionCountByWeekListToItemList(list, today, locale)

        // Then
        val expected = listOf(
            ChartItem("W8", 2021, 1),
            ChartItem("W9", 2021, 0),
            ChartItem("W10", 2021, 12),
            ChartItem("W11", 2021, 0)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given a single gap of multiple weeks When mapped Then multiple items are added`() {
        // Given
        val list = listOf(
            ActionCountByWeek(Year.of(2021), 5, 1),
            ActionCountByWeek(Year.of(2021), 10, 12)
        )
        val today = LocalDate.of(2021, 3, 10)

        // When
        val result = mapActionCountByWeekListToItemList(list, today, locale)

        // Then
        val expected = listOf(
            ChartItem("W5", 2021, 1),
            ChartItem("W6", 2021, 0),
            ChartItem("W7", 2021, 0),
            ChartItem("W8", 2021, 0),
            ChartItem("W9", 2021, 0),
            ChartItem("W10", 2021, 12)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given multiple gaps of multiple weeks When mapped Then multiple items are added`() {
        // Given
        val list = listOf(
            ActionCountByWeek(Year.of(2021), 4, 4),
            ActionCountByWeek(Year.of(2021), 8, 1),
            ActionCountByWeek(Year.of(2021), 10, 12)
        )
        val today = LocalDate.of(2021, 3, 10)

        // When
        val result = mapActionCountByWeekListToItemList(list, today, locale)

        // Then
        val expected = listOf(
            ChartItem("W4", 2021, 4),
            ChartItem("W5", 2021, 0),
            ChartItem("W6", 2021, 0),
            ChartItem("W7", 2021, 0),
            ChartItem("W8", 2021, 1),
            ChartItem("W9", 2021, 0),
            ChartItem("W10", 2021, 12)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given multiple week gaps spanning across years and empty current week When mapped Then correct number of items are added`() {
        // Given
        val list = listOf(
            ActionCountByWeek(Year.of(2020), 50, 50),
            ActionCountByWeek(Year.of(2021), 3, 3),
        )
        val today = LocalDate.of(2021, 1, 26)

        // When
        val result = mapActionCountByWeekListToItemList(list, today, locale)

        // Then
        val expected = listOf(
            ChartItem("W50", 2020, 50),
            ChartItem("W51", 2020, 0),
            ChartItem("W52", 2020, 0),
            ChartItem("W53", 2020, 0),
            ChartItem("W1", 2021, 0),
            ChartItem("W2", 2021, 0),
            ChartItem("W3", 2021, 3),
            ChartItem("W4", 2021, 0),
        )
        assertEquals(expected, result)
    }
}