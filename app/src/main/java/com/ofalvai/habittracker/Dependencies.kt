package com.ofalvai.habittracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.ofalvai.habittracker.persistence.AppDatabase
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
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

    val dashboardViewModelFactory = DashboardViewModelFactory(db.habitDao(), coroutineScope)

}

@Suppress("UNCHECKED_CAST")
class DashboardViewModelFactory(
    private val habitDao: HabitDao,
    private val coroutineScope: CoroutineScope
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DashboardViewModel(habitDao, coroutineScope) as T
    }
}