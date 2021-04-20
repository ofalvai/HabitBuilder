package com.ofalvai.habittracker.persistence

import androidx.room.TypeConverter
import com.ofalvai.habittracker.persistence.entity.Habit
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class EntityTypeConverters {

    @TypeConverter
    fun toInstant(epochMillis: Long): Instant = Instant.ofEpochMilli(epochMillis)

    @TypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun toColor(colorString: String): Habit.Color = Habit.Color.valueOf(colorString)

    @TypeConverter
    fun fromColor(color: Habit.Color): String = color.toString()

    @TypeConverter
    fun toDate(dateString: String?): LocalDate? = if (dateString == null) null else LocalDate.parse(dateString)

    @TypeConverter
    fun fromDate(localDate: LocalDate): String = localDate.toString()

    @TypeConverter
    fun toDayOfWeek(dayIndex: Int): DayOfWeek {
        // SQLite day of week: 0-6 with Sunday == 0
        return DayOfWeek.of(if (dayIndex == 0) 7 else dayIndex)
    }
}