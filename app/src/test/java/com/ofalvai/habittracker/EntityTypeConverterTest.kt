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

import com.ofalvai.habittracker.persistence.EntityTypeConverters
import com.ofalvai.habittracker.persistence.entity.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class EntityTypeConverterTest {

    private val converter = EntityTypeConverters()

    @Test
    fun `Given epoch millis When converted to Instant Then result is correct`() {
        // When
        val result = converter.toInstant(1624563468000)

        // Then
        assertEquals(Instant.ofEpochSecond(1624563468), result)
    }

    @Test
    fun `Given Instant When converted to epoch millis Then result is correct`() {
        // When
        val result = converter.fromInstant(Instant.ofEpochSecond(1624563468))

        // Then
        assertEquals(1624563468000, result)
    }

    @Test
    fun `Given color string When converted to Color Then enum is correct`() {
        // When
        val result = converter.toColor("Red")

        // Then
        assertEquals(Habit.Color.Red, result)
    }

    @Test
    fun `Given Color enum When converted to string Then result is correct`() {
        // When
        val result = converter.fromColor(Habit.Color.Yellow)

        // Then
        assertEquals("Yellow", result)
    }

    @Test
    fun `Given ISO date string When converted to LocalDate Then result is correct`() {
        // When
        val result = converter.toDate("2021-06-24")

        // Then
        assertEquals(LocalDate.of(2021, 6, 24), result)
    }

    @Test
    fun `Given null date string When converted to LocalDate Then result is null`() {
        // When
        val result = converter.toDate(null)

        // Then
        assertNull(result)
    }

    @Test
    fun `Given LocalDate object When converted to String Then result is in ISO format`() {
        // When
        val result = converter.fromDate(LocalDate.of(2020, 6, 1))

        // Then
        assertEquals("2020-06-01", result)
    }

    @Test
    fun `Given day of week indices When converted to DayOfWeek Then results are correct`() {
        assertEquals(DayOfWeek.SUNDAY, converter.toDayOfWeek(0))
        assertEquals(DayOfWeek.MONDAY, converter.toDayOfWeek(1))
        assertEquals(DayOfWeek.TUESDAY, converter.toDayOfWeek(2))
        assertEquals(DayOfWeek.WEDNESDAY, converter.toDayOfWeek(3))
        assertEquals(DayOfWeek.THURSDAY, converter.toDayOfWeek(4))
        assertEquals(DayOfWeek.FRIDAY, converter.toDayOfWeek(5))
        assertEquals(DayOfWeek.SATURDAY, converter.toDayOfWeek(6))
    }
}