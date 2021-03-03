package com.ofalvai.habittracker

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.ofalvai.habittracker.persistence.AppDatabase
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.HabitViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object Dependencies {

    private val db = Room.databaseBuilder(
        HabitTrackerApplication.INSTANCE.applicationContext,
        AppDatabase::class.java,
        "app-db"
    ).build()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HabitTrackerApplication.INSTANCE)
    private val appPreferences = AppPreferences(sharedPreferences)

    val viewModelFactory = AppViewModelFactory(db.habitDao(), coroutineScope, appPreferences)

}

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val habitDao: HabitDao,
    private val coroutineScope: CoroutineScope,
    private val appPreferences: AppPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (HabitViewModel::class.java.isAssignableFrom(modelClass)) {
            return HabitViewModel(habitDao, coroutineScope, appPreferences) as T
        }
        throw IllegalArgumentException("No matching ViewModel for ${modelClass.canonicalName}")
    }
}