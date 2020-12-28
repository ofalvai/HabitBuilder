package com.ofalvai.habittracker.persistence

import androidx.room.TypeConverter
import com.ofalvai.habittracker.persistence.entity.Habit
import java.time.Instant

class EntityTypeConverters {

    @TypeConverter
    fun toInstant(epochMillis: Long): Instant = Instant.ofEpochMilli(epochMillis)

    @TypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun toColor(colorString: String): Habit.Color = Habit.Color.valueOf(colorString)

    @TypeConverter
    fun fromColor(color: Habit.Color): String = color.toString()

}