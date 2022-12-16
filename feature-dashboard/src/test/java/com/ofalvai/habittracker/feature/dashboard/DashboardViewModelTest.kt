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

package com.ofalvai.habittracker.feature.dashboard

import app.cash.turbine.test
import com.ofalvai.habittracker.core.common.AppPreferences
import com.ofalvai.habittracker.core.common.OnboardingManager
import com.ofalvai.habittracker.core.common.Telemetry
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.ActionHistory
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.core.testing.MainCoroutineRule
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.feature.dashboard.repo.ActionRepository
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.DashboardEvent
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.ItemMoveEvent
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant
import com.ofalvai.habittracker.core.database.entity.Action as ActionEntity
import com.ofalvai.habittracker.core.database.entity.Habit as HabitEntity
import com.ofalvai.habittracker.core.database.entity.Habit.Color as ColorEntity
import com.ofalvai.habittracker.core.database.entity.HabitWithActions as HabitWithActionsEntity

class DashboardViewModelTest {

    private val dao = mock<HabitDao>()
    private val repo = mock<ActionRepository>()
    private val appPreferences = mock<AppPreferences>()
    private val telemetry = mock<Telemetry>()
    private val onboardingManager = mock<OnboardingManager>()

    private lateinit var viewModel: DashboardViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `Given habits without actions When VM loaded Then list contains habits with empty history`() = runTest {
        // Given
        given(dao.getActiveHabitsWithActions()).willReturn(flowOf(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green, 0, false, ""), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green, 1, false, ""), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green, 2, false, ""), emptyList())
        )))

        // When
        viewModel = createViewModel()

        // Then
        viewModel.habitsWithActions.test {
            val expectedActionHistory = (1..30).map { Action(0, false, null) }.toImmutableList()
            val expectedHabits = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(Result.Success(expectedHabits), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Given habits and actions in DB When Flow is observed Then collector is notified once`() = runTest {
        // Given
        given(dao.getActiveHabitsWithActions()).willReturn(flowOf((listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green, 0, false, ""), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green, 1, false, ""), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green, 2, false, ""), emptyList())
        ))))

        // When
        viewModel = createViewModel()

        // Then
        viewModel.habitsWithActions.test {
            val expectedActionHistory = (1..30).map { Action(0, false, null) }.toImmutableList()
            val expectedHabits = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(Result.Success(expectedHabits), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Given observed habit list When a habit is updated Then collector is notified`() = runTest {
        // Given
        val instantNow = Instant.now()
        val mockFlow = MutableSharedFlow<List<HabitWithActionsEntity>>(replay = 0)
        given(dao.getActiveHabitsWithActions()).willReturn(mockFlow)

        val initialHabitWithActions = listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green, 0, false, ""), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green, 1, false, ""), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green, 2, false, ""), emptyList())
        )
        val modifiedHabitWithActions = initialHabitWithActions.mapIndexed { index, habit ->
            if (index == 0) {
                habit.copy(actions = listOf(ActionEntity(0, 0, instantNow)))
            } else {
                habit
            }
        }

        // When
        viewModel = createViewModel()

        // Then
        viewModel.habitsWithActions.test {
            mockFlow.emit(initialHabitWithActions)

            val expectedActionHistory = (1..30).map { Action(0, false, null) }.toImmutableList()
            val expectedHabits1 = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(Result.Success(expectedHabits1), awaitItem())

            viewModel.toggleAction(0, Action(0, true, instantNow), 0)
            mockFlow.emit(modifiedHabitWithActions)

            val expectedActionHistory2 = expectedActionHistory.take(29) + Action(0, true, instantNow)
            val expectedHabits2 = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green, ""), expectedActionHistory2.toImmutableList(), 1, ActionHistory.Streak(1)),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green, ""), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(Result.Success(expectedHabits2), awaitItem())
        }
    }

    @Test
    fun `Given exception when toggling action When action is toggled Then error event is sent to UI`() = runTest {
        // Given
        val exception = RuntimeException("Mocked error")
        given(repo.toggleAction(any(), any(), any())).willThrow(exception)
        viewModel = createViewModel()

        // When
        viewModel.dashboardEvent.test {
            val action = Action(id = 0, toggled = true, timestamp = Instant.EPOCH)
            viewModel.toggleAction(habitId = 0, action = action, daysInPast = 2)

            // Then
            assertEquals(DashboardEvent.ActionPerformed, awaitItem())
            assertEquals(DashboardEvent.ToggleActionError, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Given exception when loading habits When habits are loaded Then ViewModel state is Failure`() = runTest {
        // Given
        val exception = RuntimeException("Mocked error")
        val habitFlow = flow<List<HabitWithActionsEntity>> {
            throw exception
        }
        given(dao.getActiveHabitsWithActions()).willReturn(habitFlow)

        // When
        viewModel = createViewModel()

        // Then
        viewModel.habitsWithActions.test {
            val expected = Result.Failure(exception)
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Given non-continuous item orders When moving an item Then it is persisted in the DB`() = runTest {
        // Given
        val habit1 = HabitEntity(id = 1, name = "First habit", color = ColorEntity.Yellow, order = 0, false, "")
        val habit2 = HabitEntity(id = 2, name = "Second habit", color = ColorEntity.Red, order = 9, false, "")
        given(dao.getHabitPair(1, 2)).willReturn(listOf(habit1, habit2))

        val updateNotificationFlow = MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST // Allow tryEmit() to succeed
        )
        given(dao.updateHabitOrders(any(), any(), any(), any())).will {
            updateNotificationFlow.tryEmit(Unit)
        }

        viewModel = createViewModel()

        updateNotificationFlow.test {
            // When
            val event = ItemMoveEvent(firstHabitId = 1, secondHabitId = 2)
            viewModel.persistItemMove(event)

            // Then
            skipItems(1)
            verifyNoInteractions(telemetry) // No exceptions
            verify(dao).updateHabitOrders(
                id1 = 1,
                order1 = 9,
                id2 = 2,
                order2 = 0
            )
        }
    }

    @Test
    fun `When moving multiple items Then they are persisted in consistent order to the DB`() = runTest {
        // Given
        val habit1 = HabitEntity(id = 1, name = "First habit", color = ColorEntity.Yellow, order = 0, false, "")
        val habit2 = HabitEntity(id = 2, name = "Second habit", color = ColorEntity.Red, order = 1, false, "")
        val habit3 = HabitEntity(id = 3, name = "Third habit", color = ColorEntity.Red, order = 2, false, "")
        given(dao.getHabitPair(1, 2)).willReturn(listOf(habit1, habit2))
        given(dao.getHabitPair(1, 3)).willReturn(listOf(habit1.copy(order = 1), habit3))

        val updateNotificationFlow = MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST // Allow tryEmit() to succeed
        )
        given(dao.updateHabitOrders(any(), any(), any(), any())).will {
            updateNotificationFlow.tryEmit(Unit)
        }

        viewModel = createViewModel()

        updateNotificationFlow.test {
            // When
            val event1 = ItemMoveEvent(firstHabitId = 1, secondHabitId = 2)
            viewModel.persistItemMove(event1)
            val event2 = ItemMoveEvent(firstHabitId = 1, secondHabitId = 3)
            viewModel.persistItemMove(event2)

            // Then
            skipItems(1)
            verify(dao).updateHabitOrders(
                id1 = 1,
                order1 = 1,
                id2 = 2,
                order2 = 0
            )

            skipItems(1)
            verify(dao).updateHabitOrders(
                id1 = 1,
                order1 = 2,
                id2 = 3,
                order2 = 1
            )

            verifyNoInteractions(telemetry) // No exceptions
        }
    }

    @Test
    fun `Given error in handling the event When moving an item Then error is handled and processing continues`() = runTest {
        // Given
        val habit1 = HabitEntity(id = 5, name = "First habit", color = ColorEntity.Yellow, order = 0, false, "")
        val habit2 = HabitEntity(id = 6, name = "Second habit", color = ColorEntity.Red, order = 1, false, "")
        given(dao.getHabitPair(5, 6)).willReturn(listOf(habit1, habit2))

        val updateNotificationFlow = MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST // Allow tryEmit() to succeed
        )
        var invocations = 0
        given(dao.updateHabitOrders(any(), any(), any(), any())).will {
            updateNotificationFlow.tryEmit(Unit)
            invocations++
            if (invocations == 1) {
                throw RuntimeException("Mocked error")
            }
        }

        viewModel = createViewModel()

        updateNotificationFlow.test {
            // When
            val event = ItemMoveEvent(firstHabitId = 5, secondHabitId = 6)
            viewModel.persistItemMove(event)

            // Then
            skipItems(1)
            verify(telemetry).logNonFatal(any())

            // Try another, non-throwing operation
            viewModel.persistItemMove(ItemMoveEvent(firstHabitId = 5, secondHabitId = 6))

            skipItems(1)
            verifyNoMoreInteractions(telemetry)
            verify(dao, times(2)).updateHabitOrders(
                id1 = 5,
                order1 = 1,
                id2 = 6,
                order2 = 0
            )
        }
    }

    private fun createViewModel() = DashboardViewModel(dao, repo, appPreferences, telemetry, onboardingManager)
}