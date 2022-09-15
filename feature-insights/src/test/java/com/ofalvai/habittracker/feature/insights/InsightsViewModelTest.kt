/*
 * Copyright 2022 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ofalvai.habittracker.feature.insights

import app.cash.turbine.test
import com.ofalvai.habittracker.core.common.OnboardingManager
import com.ofalvai.habittracker.core.common.Telemetry
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.database.entity.SumActionCountByDay
import com.ofalvai.habittracker.core.testing.MainCoroutineRule
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.feature.insights.model.HeatmapMonth
import com.ofalvai.habittracker.feature.insights.ui.InsightsViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.time.YearMonth

class InsightsViewModelTest {

    private val dao = mock<HabitDao>()
    private val telemetry = mock<Telemetry>()
    private val onboardingManager = mock<OnboardingManager>()

    private lateinit var viewModel: InsightsViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `Given habit and actions When ViewModel loads Then habit count and heatmap data are combined into ViewModel state once`() = runTest {
        // Given
        val habitCountFlow = MutableStateFlow(1)
        given(dao.getTotalHabitCount()).willReturn(habitCountFlow)
        given(dao.getSumActionCountByDay(any(), any())).willReturn(listOf(
            SumActionCountByDay(date = LocalDate.now(), action_count = 1)
        ))
        given(dao.getMostSuccessfulHabits(any())).willReturn(emptyList())
        given(dao.getTopDayForHabits()).willReturn(emptyList())

        // When
        viewModel = InsightsViewModel(dao, telemetry, onboardingManager)

        // Then
        viewModel.heatmapState.test {
            assertEquals(Result.Loading, awaitItem())
            assertEquals(1, (awaitItem() as Result.Success).value.totalHabitCount)
        }
    }

    @Test
    fun `Given loaded ViewModel When habit count changes and heatmap reloaded Then ViewModel state is updated once with new habit count`() = runTest {
        // Given
        val habitCountFlow = MutableStateFlow(1)
        given(dao.getTotalHabitCount()).willReturn(habitCountFlow)
        given(dao.getSumActionCountByDay(any(), any())).willReturn(listOf(
            SumActionCountByDay(date = LocalDate.now(), action_count = 1)
        ))
        given(dao.getMostSuccessfulHabits(any())).willReturn(emptyList())
        given(dao.getTopDayForHabits()).willReturn(emptyList())

        // When
        viewModel = createViewModel()

        // Then
        viewModel.heatmapState.test {
            assertEquals(Result.Loading, awaitItem())
            val loadedState = awaitItem() as Result.Success
            assertEquals(1, loadedState.value.totalHabitCount)

            habitCountFlow.value = 2
            viewModel.fetchHeatmap(YearMonth.now().plusMonths(1))

            val newLoadedState = awaitItem() as Result.Success
            assertEquals(2, newLoadedState.value.totalHabitCount)
        }
    }

    @Test
    fun `Given exception in heatmap loading When heatmap data is fetched Then ViewModel state is Failure`() = runTest {
        // Given
        val exception = RuntimeException("Mocked exception")
        given(dao.getSumActionCountByDay(any(), any())).willThrow(exception)
        given(dao.getTotalHabitCount()).willReturn(flowOf(1))
        given(dao.getMostSuccessfulHabits(any())).willReturn(emptyList())
        given(dao.getTopDayForHabits()).willReturn(emptyList())

        // When
        viewModel = createViewModel()

        // Then
        viewModel.heatmapState.test {
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Failure(exception), awaitItem())
        }
        viewModel.topHabits.test {
            assertEquals(Result.Success(emptyList<HeatmapMonth>()), awaitItem())
        }
        viewModel.habitTopDays.test {
            assertEquals(Result.Success(emptyList<HeatmapMonth>()), awaitItem())
        }
    }

    @Test
    fun `Given exception in top habits loading When top habits are fetched Then ViewModel state is Failure`() = runTest {
        // Given
        val exception = RuntimeException("Mocked exception")
        given(dao.getSumActionCountByDay(any(), any())).willReturn(emptyList())
        given(dao.getTotalHabitCount()).willReturn(flowOf(1))
        given(dao.getMostSuccessfulHabits(any())).willThrow(exception)
        given(dao.getTopDayForHabits()).willReturn(emptyList())

        // When
        viewModel = createViewModel()

        // Then
        viewModel.topHabits.test {
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Failure(exception), awaitItem())
        }
        viewModel.heatmapState.test {
            val expected = Result.Success(HeatmapMonth(
                yearMonth = YearMonth.now(),
                dayMap = persistentMapOf(),
                totalHabitCount = 1,
                bucketCount = 2,
                bucketMaxValues = persistentListOf(0 to 0, 1 to 1)
            ))
            assertEquals(expected, awaitItem())
        }
        viewModel.habitTopDays.test {
            assertEquals(Result.Success(emptyList<HeatmapMonth>()), awaitItem())
        }
    }

    @Test
    fun `Given exception in top days loading When top days are fetched Then ViewModel state is Failure`() = runTest {
        // Given
        val exception = RuntimeException("Mocked exception")
        given(dao.getSumActionCountByDay(any(), any())).willReturn(emptyList())
        given(dao.getTotalHabitCount()).willReturn(flowOf(1))
        given(dao.getMostSuccessfulHabits(any())).willReturn(emptyList())
        given(dao.getTopDayForHabits()).willThrow(exception)

        // When
        viewModel = createViewModel()

        // Then
        viewModel.habitTopDays.test {
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Failure(exception), awaitItem())
        }
        viewModel.topHabits.test {
            assertEquals(Result.Success(emptyList<HeatmapMonth>()), awaitItem())
        }
        viewModel.heatmapState.test {
            val expected = Result.Success(HeatmapMonth(
                yearMonth = YearMonth.now(),
                dayMap = persistentMapOf(),
                totalHabitCount = 1,
                bucketCount = 2,
                bucketMaxValues = persistentListOf(0 to 0, 1 to 1)
            ))
            assertEquals(expected, awaitItem())
        }
    }

    private fun createViewModel() = InsightsViewModel(dao, telemetry, onboardingManager)
}