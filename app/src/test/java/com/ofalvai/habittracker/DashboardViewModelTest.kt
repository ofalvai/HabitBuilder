package com.ofalvai.habittracker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.HabitViewModel
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.ActionHistory
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import com.ofalvai.habittracker.util.testObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity
import com.ofalvai.habittracker.persistence.entity.Habit as HabitEntity
import com.ofalvai.habittracker.persistence.entity.Habit.Color as ColorEntity
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    private val dao = mock<HabitDao>()
    private val appPreferences = mock<AppPreferences>()

    private lateinit var viewModel: HabitViewModel

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineScope = TestCoroutineScope()

    @Test
    fun `Given habits without actions When VM loaded Then list contains habits with empty history`() {
        given(dao.getHabitsWithActions()).willReturn(MutableLiveData(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green), emptyList())
        ))
        )

        viewModel = HabitViewModel(dao, testCoroutineScope, appPreferences)
        viewModel.habitsWithActions.testObserver()

        val expectedActionHistory = (1..7).map { Action(0, false, null) }
        val expectedHabits = listOf(
            HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
            HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
            HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
        )
        assertEquals(expectedHabits, viewModel.habitsWithActions.value)
    }

    @Test
    fun `Given habits and actions in DB When LiveData is observed Then observer is notified once`() {
        given(dao.getHabitsWithActions()).willReturn(MutableLiveData(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green), emptyList())
        ))
        )

        viewModel = HabitViewModel(dao, testCoroutineScope, appPreferences)
        val observer = viewModel.habitsWithActions.testObserver()

        val expectedActionHistory = (1..7).map { Action(0, false, null) }
        val expectedHabits = listOf(
            HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
            HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
            HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
        )
        assertEquals(1, observer.observedValues.size)
        assertEquals(expectedHabits, observer.observedValues.first())
    }

    @Test
    fun `Given observed habit list When a habit is updated Then observer is notified`() = testCoroutineScope.runBlockingTest {
        // Given
        val dateNow = LocalDate.now()
        val instantNow = Instant.now()
        val daoLiveData = MutableLiveData(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green), emptyList())
        ))
        given(dao.getHabitsWithActions()).willReturn(daoLiveData)

        viewModel = HabitViewModel(dao, testCoroutineScope, appPreferences)
        val observer = viewModel.habitsWithActions.testObserver()

        // When
        viewModel.toggleActionFromDashboard(0, Action(0, true, instantNow), dateNow)
        daoLiveData.value = daoLiveData.value!!.mapIndexed { index, habit ->
            if (index == 0) {
                habit.copy(actions = listOf(ActionEntity(0, 0, instantNow)))
            } else {
                habit
            }
        }

        // Then
        val expectedActionHistory = (1..7).map { Action(0, false, null) }
        val expectedHabits1 = listOf(
            HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
            HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
            HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
        )
        val expectedHabits2 = listOf(
            HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory.take(6) + Action(0, true, instantNow), 1, ActionHistory.Streak(1)),
            HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
            HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
        )
        assertEquals(2, observer.observedValues.size)
        assertEquals(expectedHabits1, observer.observedValues[0])
        assertEquals(expectedHabits2, observer.observedValues[1])
    }
}