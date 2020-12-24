package com.ofalvai.habittracker.persistence

import androidx.room.TypeConverter
import java.time.Instant

class EntityTypeConverters {

    @TypeConverter
    fun toInstant(epochMillis: Long): Instant = Instant.ofEpochMilli(epochMillis)

    @TypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilli()

}