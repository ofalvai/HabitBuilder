package com.ofalvai.habittracker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.HabitViewModel
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
import com.ofalvai.habittracker.persistence.entity.Habit.Color as ColorEntity
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    private val dao = mock<HabitDao>()

    private lateinit var viewModel: HabitViewModel

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineScope = TestCoroutineScope()

    @Test
    fun `Given habits without actions When VM loaded Then list contains habits with empty history`() = testCoroutineScope.runBlockingTest {
        given(dao.getHabitsWithActions()).willReturn(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green), emptyList())
        ))

        viewModel = HabitViewModel(dao, testCoroutineScope)

        val expectedActionHistory = (1..5).map { Action(0, false, null) }
        val expectedHabits = listOf(
            HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0),
            HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0),
            HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0)
        )
        assertEquals(expectedHabits, viewModel.habitsWithActions.value)
    }

    @Test
    fun `Given habit with actions in the last 5 days When VM loaded Then actions are visible`() = testCoroutineScope.runBlockingTest {
        val now = Instant.now()
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = now),
            ActionEntity(id = 2, habit_id = 0, timestamp = now.minus(1, ChronoUnit.DAYS)),
            ActionEntity(id = 3, habit_id = 0, timestamp = now.minus(3, ChronoUnit.DAYS))
        )
        given(dao.getHabitsWithActions()).willReturn(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), actions),
        ))

        viewModel = HabitViewModel(dao, testCoroutineScope)

        val expectedActionHistory = listOf(
            Action(0, false, null),
            Action(3, true, now.minus(3, ChronoUnit.DAYS)),
            Action(0, false, null),
            Action(2, true, now.minus(1, ChronoUnit.DAYS)),
            Action(1, true, now)
        )
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Green),
            expectedActionHistory,
            3
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
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), actions),
        ))

        viewModel = HabitViewModel(dao, testCoroutineScope)

        val expectedActionHistory = (1..5).map { Action(0, false, null) }
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Green),
            expectedActionHistory,
            2
        ))
        assertEquals(expectedHabits, viewModel.habitsWithActions.value)
    }

    @Test
    fun `Given habit with action in last 5 days and before When VM loaded Then only actions from last 5 days are visible`() = testCoroutineScope.runBlockingTest {
        val now = Instant.now()
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = now),
            ActionEntity(id = 2, habit_id = 0, timestamp = now.minus(3, ChronoUnit.DAYS)),
            ActionEntity(id = 3, habit_id = 0, timestamp = now.minus(1, ChronoUnit.DAYS)),
            ActionEntity(id = 4, habit_id = 0, timestamp = now.minus(5, ChronoUnit.DAYS)),
            ActionEntity(id = 5, habit_id = 0, timestamp = now.minus(19, ChronoUnit.DAYS))
        )
        given(dao.getHabitsWithActions()).willReturn(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), actions),
        ))

        viewModel = HabitViewModel(dao, testCoroutineScope)

        val expectedActionHistory = listOf(
            Action(0, false, null),
            Action(2, true, now.minus(3, ChronoUnit.DAYS)),
            Action(0, false, null),
            Action(3, true, now.minus(1, ChronoUnit.DAYS)),
            Action(1, true, now)
        )
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Green),
            expectedActionHistory,
            5
        ))
        assertEquals(expectedHabits, viewModel.habitsWithActions.value)
    }
}