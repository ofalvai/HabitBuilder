package com.ofalvai.habittracker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.persistence.entity.SumActionCountByDay
import com.ofalvai.habittracker.ui.insights.InsightsViewModel
import com.ofalvai.habittracker.util.MainCoroutineRule
import com.ofalvai.habittracker.util.testObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@ExperimentalCoroutinesApi
class InsightsViewModelTest {

    private val dao = mock<HabitDao>()

    private lateinit var viewModel: InsightsViewModel

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `Given habit and actions When ViewModel loads Then habit count and heatmap data are combined into ViewModel state once`() = runBlockingTest {
        // Given
        val habitCountFlow = MutableStateFlow(1)
        given(dao.getHabitCount()).willReturn(habitCountFlow)
        given(dao.getSumActionCountByDay()).willReturn(listOf(
            SumActionCountByDay(date = LocalDate.now(), action_count = 1)
        ))

        // When
        viewModel = InsightsViewModel(dao)
        val observer = viewModel.heatmapData.testObserver()

        // Then
        assertEquals(1, observer.observedValues.size)
        assertEquals(1, observer.observedValues[0]!!.totalHabitCount)
    }

    @Test
    fun `Given loaded ViewModel When habit count changes and heatmap reloaded Then ViewModel state is updated once with new habit count`() = runBlockingTest {
        // Given
        val habitCountFlow = MutableStateFlow(1)
        given(dao.getHabitCount()).willReturn(habitCountFlow)
        given(dao.getSumActionCountByDay()).willReturn(listOf(
            SumActionCountByDay(date = LocalDate.now(), action_count = 1)
        ))

        // When
        viewModel = InsightsViewModel(dao)
        val observer = viewModel.heatmapData.testObserver()
        habitCountFlow.value = 2
        viewModel.fetchHeatmap(YearMonth.now().plusMonths(1))

        // Then
        assertEquals(2, observer.observedValues.size)
        assertEquals(1, observer.observedValues[0]!!.totalHabitCount)
        assertEquals(2, observer.observedValues[1]!!.totalHabitCount)
    }
}