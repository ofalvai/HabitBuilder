package com.ofalvai.habittracker

import android.app.Application
import timber.log.Timber

class HabitTrackerApplication : Application() {

    companion object {
        lateinit var INSTANCE: HabitTrackerApplication
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}