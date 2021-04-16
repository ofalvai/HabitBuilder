package com.ofalvai.habittracker

import android.preference.PreferenceManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.ofalvai.habittracker.persistence.AppDatabase
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.insights.InsightsViewModel

object Dependencies {

    private val db = Room.databaseBuilder(
        HabitTrackerApplication.INSTANCE.applicationContext,
        AppDatabase::class.java,
        "app-db"
    ).build()

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HabitTrackerApplication.INSTANCE)
    private val appPreferences = AppPreferences(sharedPreferences)

    val viewModelFactory = AppViewModelFactory(db.habitDao(), appPreferences)
}

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val habitDao: HabitDao,
    private val appPreferences: AppPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (HabitViewModel::class.java.isAssignableFrom(modelClass)) {
            return HabitViewModel(habitDao, appPreferences) as T
        }
        if (InsightsViewModel::class.java.isAssignableFrom(modelClass)) {
            return InsightsViewModel(habitDao) as T
        }
        throw IllegalArgumentException("No matching ViewModel for ${modelClass.canonicalName}")
    }
}