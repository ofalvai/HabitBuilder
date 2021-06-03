package com.ofalvai.habittracker.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.mapHabitActionCount
import com.ofalvai.habittracker.mapper.mapHabitTopDay
import com.ofalvai.habittracker.mapper.mapSumActionCountByDay
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.model.HeatmapMonth
import com.ofalvai.habittracker.ui.model.TopDayItem
import com.ofalvai.habittracker.ui.model.TopHabitItem
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class InsightsViewModel(
    private val habitDao: HabitDao
): ViewModel() {

    val heatmapState = MutableStateFlow<Result<HeatmapMonth>>(Result.Loading)
    val topHabits = MutableStateFlow<Result<List<TopHabitItem>>>(Result.Success(emptyList()))
    val habitTopDays = MutableStateFlow<Result<List<TopDayItem>>>(Result.Success(emptyList()))

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
            @Suppress("DeferredResultUnused")
            async { reloadHeatmap(yearMonth = YearMonth.now()) }

            @Suppress("DeferredResultUnused")
            async { reloadTopHabits() }

            @Suppress("DeferredResultUnused")
            async { reloadHabitTopDays() }
        }
    }

    private suspend fun reloadHeatmap(yearMonth: YearMonth) {
        heatmapState.value = Result.Loading

        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        val actionCountList = habitDao.getSumActionCountByDay(startDate, endDate)
        val habitCount = habitCount.first()

        heatmapState.value = Result.Success(
            mapSumActionCountByDay(
                entityList = actionCountList,
                yearMonth,
                habitCount
            )
        )
    }

    private suspend fun reloadTopHabits() {
        topHabits.value = Result.Loading
        topHabits.value = habitDao
            .getMostSuccessfulHabits(100) // TODO: smaller number when "See all" screen is done
            .filter { it.first_day != null }
            .map { mapHabitActionCount(it, LocalDate.now()) }
            .let { Result.Success(it) }
    }

    private suspend fun reloadHabitTopDays() {
        habitTopDays.value = Result.Loading
        habitTopDays.value = habitDao
            .getTopDayForHabits()
            .map { mapHabitTopDay(it) }
            .let { Result.Success(it) }
    }
}