package com.ofalvai.habittracker.ui.insights

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.mapSumActionCountByDay
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.persistence.entity.HabitActionCount
import com.ofalvai.habittracker.persistence.entity.HabitTopDay
import com.ofalvai.habittracker.ui.model.HeatmapMonth
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class InsightsViewModel(
    private val habitDao: HabitDao
): ViewModel() {

    val heatmapData = MutableLiveData<HeatmapMonth>()

    // TODO: map entities to models
    val mostSuccessfulHabits = MutableLiveData<List<HabitActionCount>>()
    val habitTopDays = MutableLiveData<List<HabitTopDay>>()

    private val habitCount: SharedFlow<Int> = habitDao.getHabitCount().shareIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1
    )

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
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        val actionCountList = habitDao.getSumActionCountByDay(startDate, endDate)
        val habitCount = habitCount.first()
        heatmapData.value = mapSumActionCountByDay(
            entityList = actionCountList,
            yearMonth,
            habitCount
        )
    }
}