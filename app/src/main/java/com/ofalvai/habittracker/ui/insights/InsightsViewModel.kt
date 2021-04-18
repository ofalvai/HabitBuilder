package com.ofalvai.habittracker.ui.insights

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.mapSumActionCountByDay
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.persistence.entity.HabitActionCount
import com.ofalvai.habittracker.persistence.entity.HabitTopDay
import com.ofalvai.habittracker.ui.model.HeatmapMonth
import kotlinx.coroutines.launch
import java.time.YearMonth

class InsightsViewModel(
    private val habitDao: HabitDao
): ViewModel() {

    val heatmapData = MutableLiveData<HeatmapMonth>()
    val mostSuccessfulHabits = MutableLiveData<List<HabitActionCount>>()
    val habitTopDays = MutableLiveData<List<HabitTopDay>>()

    init {
        fetchStats()
    }

    fun fetchHeatmap(yearMonth: YearMonth) {
        viewModelScope.launch {
            reloadHeatmap(yearMonth)
        }
    }

    private fun fetchStats() {
        viewModelScope.launch {
            fetchHeatmap(yearMonth = YearMonth.now())
            mostSuccessfulHabits.value = habitDao.getMostSuccessfulHabits(10)
            habitTopDays.value = habitDao.getTopDayForHabits()
        }
    }

    private suspend fun reloadHeatmap(yearMonth: YearMonth) {
        heatmapData.value = mapSumActionCountByDay(habitDao.getSumActionCountByDay(), yearMonth)
    }
}