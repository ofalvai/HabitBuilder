package com.ofalvai.habittracker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.ActionHistory
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import com.ofalvai.habittracker.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.time.ExperimentalTime
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity
import com.ofalvai.habittracker.persistence.entity.Habit as HabitEntity
import com.ofalvai.habittracker.persistence.entity.Habit.Color as ColorEntity
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

@ExperimentalTime
@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    private val dao = mock<HabitDao>()
    private val appPreferences = mock<AppPreferences>()

    private lateinit var viewModel: DashboardViewModel

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `Given habits without actions When VM loaded Then list contains habits with empty history`() = runBlockingTest {
        // Given
        given(dao.getHabitsWithActions()).willReturn(flowOf(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green), emptyList())
        )))

        // When
        viewModel = DashboardViewModel(dao, appPreferences)

        // Then
        viewModel.habitsWithActions.test {
            val expectedActionHistory = (1..7).map { Action(0, false, null) }
            val expectedHabits = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(expectedHabits, expectItem())
            expectComplete()
        }
    }

    @Test
    fun `Given habits and actions in DB When Flow is observed Then collector is notified once`() = runBlockingTest {
        // Given
        given(dao.getHabitsWithActions()).willReturn(flowOf((listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green), emptyList())
        ))))

        // When
        viewModel = DashboardViewModel(dao, appPreferences)

        // Then
        viewModel.habitsWithActions.test {
            val expectedActionHistory = (1..7).map { Action(0, false, null) }
            val expectedHabits = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(expectedHabits, expectItem())
            expectComplete()
        }
    }

    @Test
    fun `Given observed habit list When a habit is updated Then collector is notified`() = runBlockingTest {
        // Given
        val dateNow = LocalDate.now()
        val instantNow = Instant.now()
        val mockFlow = MutableSharedFlow<List<HabitWithActionsEntity>>(replay = 0)
        given(dao.getHabitsWithActions()).willReturn(mockFlow)

        val initialHabitWithActions = listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green), emptyList())
        )
        val modifiedHabitWithActions = initialHabitWithActions.mapIndexed { index, habit ->
            if (index == 0) {
                habit.copy(actions = listOf(ActionEntity(0, 0, instantNow)))
            } else {
                habit
            }
        }

        // When
        viewModel = DashboardViewModel(dao, appPreferences)

        // Then
        viewModel.habitsWithActions.test {
            mockFlow.emit(initialHabitWithActions)

            val expectedActionHistory = (1..7).map { Action(0, false, null) }
            val expectedHabits1 = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(expectedHabits1, expectItem())

            viewModel.toggleActionFromDashboard(0, Action(0, true, instantNow), dateNow)
            mockFlow.emit(modifiedHabitWithActions)

            val expectedHabits2 = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory.take(6) + Action(0, true, instantNow), 1, ActionHistory.Streak(1)),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(expectedHabits2, expectItem())
        }
    }

    @Test
    fun `Given exception when toggling action When action is toggled Then error event is sent to UI`() = runBlockingTest {
        // Given
        val exception = RuntimeException("Mocked error")
        given(dao.insertAction()).willThrow(exception)
        viewModel = DashboardViewModel(dao, appPreferences)
        val action = Action(id = 0, toggled = true, timestamp = Instant.EPOCH)

        // When
        viewModel.toggleActionFromDashboard(habitId = 0, action = action, date = LocalDate.of(2021, 6, 7))

        // Then
        viewModel.toggleActionErrorEvent.value = exception
    }

    @Test
    fun `Given exception when loading habits When habits are loaded Then ViewModel state is Failure`() = runBlockingTest {
        // Given
        val exception = RuntimeException("Mocked error")
        val habitFlow = flow<List<HabitWithActionsEntity>> {
            throw exception
        }
        given(dao.getHabitsWithActions()).willReturn(habitFlow)

        // When
        viewModel = DashboardViewModel(dao, appPreferences)

        // Then
        viewModel.habitsWithActions.test {
            val expected = Result.Failure(exception)
            assertEquals(expected, expectItem())
            expectComplete()
        }
    }
}