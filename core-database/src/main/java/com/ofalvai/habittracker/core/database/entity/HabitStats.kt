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

package com.ofalvai.habittracker.core.database.entity

import java.time.*
import java.time.temporal.ChronoUnit

data class ActionCountByMonth(
    val year: Int,
    val month: Month,
    val action_count: Int
)

data class ActionCountByWeek(
    val year: Int,
    val week: Int,
    val action_count: Int
)

data class ActionCompletionRate(
    val first_day: Instant, // Value is Instant.EPOCH if there are no actions
    val action_count: Int
) {
    fun rateAsOf(date: LocalDate): Float {
        if (action_count == 0 || first_day == Instant.EPOCH) {
            return 0f
        }

        val firstDayDate = LocalDateTime.ofInstant(first_day, ZoneId.systemDefault()).toLocalDate()
        val daysBetweenInclusive = ChronoUnit.DAYS.between(firstDayDate, date) + 1

        return action_count / daysBetweenInclusive.toFloat()
    }
}

data class SumActionCountByDay(
    val date: LocalDate?,
    val action_count: Int
)

data class HabitActionCount(
    val habit_id: Int,
    val name: String,
    val first_day: LocalDate?, // Used for compensating for different start days among habits
    val count: Int
)

data class HabitTopDay(
    val habit_id: Int,
    val name: String,
    val top_day_of_week: DayOfWeek,
    val action_count_on_day: Int
)