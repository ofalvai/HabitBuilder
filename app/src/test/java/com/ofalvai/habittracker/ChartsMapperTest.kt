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

import com.ofalvai.habittracker.mapper.mapActionCountByMonthListToItemList
import com.ofalvai.habittracker.mapper.mapActionCountByWeekListToItemList
import com.ofalvai.habittracker.ui.habitdetail.ChartItem
import com.ofalvai.habittracker.ui.model.ActionCountByMonth
import com.ofalvai.habittracker.ui.model.ActionCountByWeek
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Year
import java.time.YearMonth

class ChartsMapperTest {

    @Test
    fun `Given consecutive months When mapped Then no filling item is added`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2021, 8), 1),
            ActionCountByMonth(YearMonth.of(2021, 9), 6),
            ActionCountByMonth(YearMonth.of(2021, 10), 12)
        )

        // When
        val result = mapActionCountByMonthListToItemList(list)

        // Then
        val expected = listOf(
            ChartItem("8", 2021, 1),
            ChartItem("9", 2021, 6),
            ChartItem("10", 2021, 12)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given empty month list When mapped Then result is empty too`() {
        // Given
        val list = emptyList<ActionCountByMonth>()

        // When
        val result = mapActionCountByMonthListToItemList(list)

        // Then
        val expected = emptyList<ChartItem>()
        assertEquals(expected, result)
    }

    @Test
    fun `Given single month When mapped Then result is single month too`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2021, 10), 12)
        )

        // When
        val result = mapActionCountByMonthListToItemList(list)

        // Then
        val expected = listOf(
            ChartItem("10", 2021, 12)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given a single gap of 1 month When mapped Then a single item is added`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2021, 8), 1),
            ActionCountByMonth(YearMonth.of(2021, 10), 12)
        )

        // When
        val result = mapActionCountByMonthListToItemList(list)

        // Then
        val expected = listOf(
            ChartItem("8", 2021, 1),
            ChartItem("9", 2021, 0),
            ChartItem("10", 2021, 12)
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

        // When
        val result = mapActionCountByMonthListToItemList(list)

        // Then
        val expected = listOf(
            ChartItem("4", 2021, 1),
            ChartItem("5", 2021, 0),
            ChartItem("6", 2021, 0),
            ChartItem("7", 2021, 0),
            ChartItem("8", 2021, 0),
            ChartItem("9", 2021, 0),
            ChartItem("10", 2021, 12)
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

        // When
        val result = mapActionCountByMonthListToItemList(list)

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

        // When
        val result = mapActionCountByMonthListToItemList(list)

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
    fun `Given multiple gaps spanning across years When mapped Then correct number of items are added`() {
        // Given
        val list = listOf(
            ActionCountByMonth(YearMonth.of(2020, 11), 11),
            ActionCountByMonth(YearMonth.of(2021, 3), 3),
            ActionCountByMonth(YearMonth.of(2021, 10), 10),
            ActionCountByMonth(YearMonth.of(2021, 12), 12),
            ActionCountByMonth(YearMonth.of(2022, 2), 2),
        )

        // When
        val result = mapActionCountByMonthListToItemList(list)

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

        // When
        val result = mapActionCountByWeekListToItemList(list)

        // Then
        val expected = listOf(
            ChartItem("8", 2021, 1),
            ChartItem("9", 2021, 6),
            ChartItem("10", 2021, 12)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given empty week list When mapped Then result is empty too`() {
        // Given
        val list = emptyList<ActionCountByWeek>()

        // When
        val result = mapActionCountByWeekListToItemList(list)

        // Then
        val expected = emptyList<ChartItem>()
        assertEquals(expected, result)
    }

    @Test
    fun `Given single week When mapped Then result is single item`() {
        // Given
        val list = listOf(ActionCountByWeek(Year.of(2021), 10, 16))

        // When
        val result = mapActionCountByWeekListToItemList(list)

        // Then
        val expected = listOf(ChartItem("10", 2021, 16))
        assertEquals(expected, result)
    }

    @Test
    fun `Given a single gap of 1 week When mapped Then a single item is added`() {
        // Given
        val list = listOf(
            ActionCountByWeek(Year.of(2021), 8, 1),
            ActionCountByWeek(Year.of(2021), 10, 12)
        )

        // When
        val result = mapActionCountByWeekListToItemList(list)

        // Then
        val expected = listOf(
            ChartItem("8", 2021, 1),
            ChartItem("9", 2021, 0),
            ChartItem("10", 2021, 12)
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

        // When
        val result = mapActionCountByWeekListToItemList(list)

        // Then
        val expected = listOf(
            ChartItem("5", 2021, 1),
            ChartItem("6", 2021, 0),
            ChartItem("7", 2021, 0),
            ChartItem("8", 2021, 0),
            ChartItem("9", 2021, 0),
            ChartItem("10", 2021, 12)
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

        // When
        val result = mapActionCountByWeekListToItemList(list)

        // Then
        val expected = listOf(
            ChartItem("4", 2021, 4),
            ChartItem("5", 2021, 0),
            ChartItem("6", 2021, 0),
            ChartItem("7", 2021, 0),
            ChartItem("8", 2021, 1),
            ChartItem("9", 2021, 0),
            ChartItem("10", 2021, 12)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Given multiple week gaps spanning across years When mapped Then correct number of items are added`() {
        // Given
        val list = listOf(
            ActionCountByWeek(Year.of(2020), 50, 50),
            ActionCountByWeek(Year.of(2021), 3, 3),
        )

        // When
        val result = mapActionCountByWeekListToItemList(list)

        // Then
        val expected = listOf(
            ChartItem("50", 2020, 50),
            ChartItem("51", 2020, 0),
            ChartItem("52", 2020, 0),
            ChartItem("53", 2020, 0),
            ChartItem("1", 2021, 0),
            ChartItem("2", 2021, 0),
            ChartItem("3", 2021, 3),
        )
        assertEquals(expected, result)
    }
}