package com.ofalvai.habittracker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity
import com.ofalvai.habittracker.persistence.entity.Habit as HabitEntity
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    private val dao = mock<HabitDao>()

    private lateinit var viewModel: DashboardViewModel

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineScope = TestCoroutineScope()

    @Test
    fun `Given habits without actions When VM loaded Then list contains habits with empty history`() = testCoroutineScope.runBlockingTest {
        given(dao.getHabitsWithActions()).willReturn(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation"), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running"), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout"), emptyList())
        ))

        viewModel = DashboardViewModel(dao, testCoroutineScope)

        val expectedActionHistory = (1..5).map { Action(0, false) }
        val expectedHabits = listOf(
            HabitWithActions(Habit(0, "Meditation"), expectedActionHistory),
            HabitWithActions(Habit(1, "Running"), expectedActionHistory),
            HabitWithActions(Habit(2, "Workout"), expectedActionHistory)
        )
        assertEquals(expectedHabits, viewModel.habitsWithActions.value)
    }

    @Test
    fun `Given habit with actions in the last 5 days When VM loaded Then actions are visible`() = testCoroutineScope.runBlockingTest {
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = Instant.now()),
            ActionEntity(id = 2, habit_id = 0, timestamp = Instant.now().minus(1, ChronoUnit.DAYS)),
            ActionEntity(id = 3, habit_id = 0, timestamp = Instant.now().minus(3, ChronoUnit.DAYS))
        )
        given(dao.getHabitsWithActions()).willReturn(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation"), actions),
        ))

        viewModel = DashboardViewModel(dao, testCoroutineScope)

        val expectedActionHistory = listOf(
            Action(0, false),
            Action(3, true),
            Action(0, false),
            Action(2, true),
            Action(1, true)
        )
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation"),
            expectedActionHistory
        ))
        assertEquals(expectedHabits, viewModel.habitsWithActions.value)
    }

    @Test
    fun `Given habit with actions before the last 5 days When VM loaded Then last 5 actions are empty`() = testCoroutineScope.runBlockingTest {
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = Instant.now().minus(10, ChronoUnit.DAYS)),
            ActionEntity(id = 2, habit_id = 0, timestamp = Instant.now().minus(19, ChronoUnit.DAYS))
        )
        given(dao.getHabitsWithActions()).willReturn(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation"), actions),
        ))

        viewModel = DashboardViewModel(dao, testCoroutineScope)

        val expectedActionHistory = (1..5).map { Action(0, false) }
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation"),
            expectedActionHistory
        ))
        assertEquals(expectedHabits, viewModel.habitsWithActions.value)
    }

    @Test
    fun `Given habit with action in last 5 days and before When VM loaded Then only actions from last 5 days are visible`() = testCoroutineScope.runBlockingTest {
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = Instant.now()),
            ActionEntity(id = 2, habit_id = 0, timestamp = Instant.now().minus(3, ChronoUnit.DAYS)),
            ActionEntity(id = 3, habit_id = 0, timestamp = Instant.now().minus(1, ChronoUnit.DAYS)),
            ActionEntity(id = 4, habit_id = 0, timestamp = Instant.now().minus(5, ChronoUnit.DAYS)),
            ActionEntity(id = 5, habit_id = 0, timestamp = Instant.now().minus(19, ChronoUnit.DAYS))
        )
        given(dao.getHabitsWithActions()).willReturn(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation"), actions),
        ))

        viewModel = DashboardViewModel(dao, testCoroutineScope)

        val expectedActionHistory = listOf(
            Action(0, false),
            Action(2, true),
            Action(0, false),
            Action(3, true),
            Action(1, true)
        )
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation"),
            expectedActionHistory
        ))
        assertEquals(expectedHabits, viewModel.habitsWithActions.value)
    }
}