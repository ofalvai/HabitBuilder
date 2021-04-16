package com.ofalvai.habittracker.ui.insights

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.persistence.entity.HabitActionCount
import com.ofalvai.habittracker.persistence.entity.HabitTopDay
import kotlinx.coroutines.launch

class InsightsViewModel(
    private val habitDao: HabitDao
): ViewModel() {

    val mostSuccessfulHabits = MutableLiveData<List<HabitActionCount>>()
    val habitTopDays = MutableLiveData<List<HabitTopDay>>()

    init {
        fetchStats()
    }

    private fun fetchStats() {
        viewModelScope.launch {
            mostSuccessfulHabits.value = habitDao.getMostSuccessfulHabits(10)
            habitTopDays.value = habitDao.getTopDayForHabits()
        }
    }


}