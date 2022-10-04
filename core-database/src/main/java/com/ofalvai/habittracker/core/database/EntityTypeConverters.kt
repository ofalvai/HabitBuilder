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

package com.ofalvai.habittracker.core.database

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Month

class EntityTypeConverters {

    @TypeConverter
    fun toInstant(epochMillis: Long): Instant = Instant.ofEpochMilli(epochMillis)

    @TypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun toDate(dateString: String?): LocalDate? = if (dateString == null) null else LocalDate.parse(dateString)

    @TypeConverter
    fun fromDate(localDate: LocalDate): String = localDate.toString()

    @TypeConverter
    fun toDayOfWeek(dayIndex: Int): DayOfWeek {
        // SQLite day of week: 0-6 with Sunday == 0
        return DayOfWeek.of(if (dayIndex == 0) 7 else dayIndex)
    }

    @TypeConverter
    fun toMonth(monthString: String): Month {
        // SQLite's strftime('%m, ...) returns month numbers with a zero padding (eg. 01, 02, 03).
        // We can't convert the returned value to Int directly because the leading zero makes
        // Kotlin (Java) parse the digits as an octal value. It works for the first 7 months,
        // then everything blows up in August...
        return Month.of(monthString.toInt())
    }
}