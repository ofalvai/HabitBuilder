package com.ofalvai.habittracker

import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.ofalvai.habittracker.persistence.AppDatabase
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.persistence.MIGRATIONS
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.dashboard.AddHabitViewModel
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailViewModel
import com.ofalvai.habittracker.ui.insights.InsightsViewModel
import com.ofalvai.habittracker.ui.settings.LicensesViewModel
import timber.log.Timber

object Dependencies {

    private val appContext = HabitTrackerApplication.INSTANCE.applicationContext

    private val db = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "app-db"
    )
        .setQueryCallback(::roomQueryLogCallback, Runnable::run)
        .addMigrations(*MIGRATIONS)
        .build()

    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(HabitTrackerApplication.INSTANCE)
    private val appPreferences = AppPreferences(sharedPreferences)

    val viewModelFactory = AppViewModelFactory(db.habitDao(), appPreferences, appContext)
}

private fun roomQueryLogCallback(sqlQuery: String, bindArgs: List<Any>) {
    Timber.tag("RoomQueryLog")
    Timber.d("Query: %s", sqlQuery)
    if (bindArgs.isNotEmpty()) {
        Timber.tag("RoomQueryLog")
        Timber.d("Args: %s", bindArgs.toString())
    }
}

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val habitDao: HabitDao,
    private val appPreferences: AppPreferences,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (AddHabitViewModel::class.java.isAssignableFrom(modelClass)) {
            return AddHabitViewModel(habitDao) as T
        }
        if (DashboardViewModel::class.java.isAssignableFrom(modelClass)) {
            return DashboardViewModel(habitDao, appPreferences) as T
        }
        if (HabitDetailViewModel::class.java.isAssignableFrom(modelClass)) {
            return HabitDetailViewModel(habitDao) as T
        }
        if (InsightsViewModel::class.java.isAssignableFrom(modelClass)) {
            return InsightsViewModel(habitDao) as T
        }
        if (LicensesViewModel::class.java.isAssignableFrom(modelClass)) {
            return LicensesViewModel(appContext) as T
        }
        throw IllegalArgumentException("No matching ViewModel for ${modelClass.canonicalName}")
    }
}