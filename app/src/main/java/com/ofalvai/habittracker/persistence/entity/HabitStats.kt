package com.ofalvai.habittracker.persistence.entity

import java.time.*
import java.time.temporal.ChronoUnit

data class ActionCountByMonth(
    val year: Int,
    val month: Int,
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
        val daysBetween = ChronoUnit.DAYS.between(firstDayDate, date)
        if (daysBetween == 0L) {
            return 1f
        }

        return action_count / daysBetween.toFloat()
    }
}

data class SumActionCountByDay(
    val date: LocalDate,
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