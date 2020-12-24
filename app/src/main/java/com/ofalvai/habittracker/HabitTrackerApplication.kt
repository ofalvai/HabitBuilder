package com.ofalvai.habittracker

import android.app.Application

class HabitTrackerApplication : Application() {

    companion object {
        lateinit var INSTANCE: HabitTrackerApplication
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this
    }
}