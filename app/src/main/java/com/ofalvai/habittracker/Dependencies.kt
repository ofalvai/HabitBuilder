package com.ofalvai.habittracker

import androidx.room.Room
import com.ofalvai.habittracker.persistence.AppDatabase

object Dependencies {

    val db = Room.databaseBuilder(
        HabitTrackerApplication.INSTANCE,
        AppDatabase::class.java,
        "app-db"
    ).build()

}