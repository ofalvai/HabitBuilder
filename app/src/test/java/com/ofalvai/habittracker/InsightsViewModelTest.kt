package com.ofalvai.habittracker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.persistence.entity.SumActionCountByDay
import com.ofalvai.habittracker.ui.insights.HeatmapState
import com.ofalvai.habittracker.ui.insights.InsightsViewModel
import com.ofalvai.habittracker.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class InsightsViewModelTest {

    private val dao = mock<HabitDao>()

    private lateinit var viewModel: InsightsViewModel

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule() // TODO: remove

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @ExperimentalTime
    @Test
    fun `Given habit and actions When ViewModel loads Then habit count and heatmap data are combined into ViewModel state once`() = runBlocking {
        // Given
        val habitCountFlow = MutableStateFlow(1)
        given(dao.getHabitCount()).willReturn(habitCountFlow)
        given(dao.getSumActionCountByDay(any(), any())).willReturn(listOf(
            SumActionCountByDay(date = LocalDate.now(), action_count = 1)
        ))
        given(dao.getMostSuccessfulHabits(any())).willReturn(emptyList())
        given(dao.getTopDayForHabits()).willReturn(emptyList())

        // When
        viewModel = InsightsViewModel(dao)

        // Then
        viewModel.heatmapState.test {
            // First item is Loading, but we only subscribe after the constructor has run
            // Which executes the fetching in a blocking way
            assertEquals(1, (expectItem() as HeatmapState.Loaded).heatmapData.totalHabitCount)
        }
    }

    @ExperimentalTime
    @Test
    fun `Given loaded ViewModel When habit count changes and heatmap reloaded Then ViewModel state is updated once with new habit count`() = runBlocking {
        // Given
        val habitCountFlow = MutableStateFlow(1)
        given(dao.getHabitCount()).willReturn(habitCountFlow)
        given(dao.getSumActionCountByDay(any(), any())).willReturn(listOf(
            SumActionCountByDay(date = LocalDate.now(), action_count = 1)
        ))
        given(dao.getMostSuccessfulHabits(any())).willReturn(emptyList())
        given(dao.getTopDayForHabits()).willReturn(emptyList())

        // When
        viewModel = InsightsViewModel(dao)

        // Then
        viewModel.heatmapState.test {
            val loadedState = expectItem() as HeatmapState.Loaded
            assertEquals(1, loadedState.heatmapData.totalHabitCount)

            habitCountFlow.value = 2
            viewModel.fetchHeatmap(YearMonth.now().plusMonths(1))

            assertEquals(HeatmapState.Loading, expectItem())
            val newLoadedState = expectItem() as HeatmapState.Loaded
            assertEquals(2, newLoadedState.heatmapData.totalHabitCount)

        }
    }
}