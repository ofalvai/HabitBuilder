package com.ofalvai.habittracker.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ofalvai.habittracker.persistence.entity.Action
import com.ofalvai.habittracker.persistence.entity.Habit

@Database(
    entities = [
        Habit::class, Action::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(EntityTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

}