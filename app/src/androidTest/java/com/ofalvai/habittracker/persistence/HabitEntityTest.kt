package com.ofalvai.habittracker.persistence

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ofalvai.habittracker.persistence.entity.Action
import com.ofalvai.habittracker.persistence.entity.Habit
import com.ofalvai.habittracker.persistence.entity.HabitWithActions
import com.ofalvai.habittracker.persistence.util.testObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class HabitEntityTest {

    private lateinit var habitDao: HabitDao
    private lateinit var db: AppDatabase
    private val testCoroutineScope = TestCoroutineScope()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        habitDao = db.habitDao()
    }

    @After
    @Throws(IOException::class)
    fun after() {
        db.close()
    }

    @Test
    fun insertAndReadHabits() = testCoroutineScope.runBlockingTest {
        val newHabit1 = Habit(name = "New habit", color = Habit.Color.Green)
        val newHabit2 = Habit(name = "Other new habit", color = Habit.Color.Green)

        habitDao.insertHabit(newHabit1, newHabit2)

        val habits = habitDao.getHabits()
        val expected = listOf(newHabit1.copy(id = 1), newHabit2.copy(id = 2))
        assertEquals(expected, habits)
    }

    @Test
    fun insertAndReadActions() = testCoroutineScope.runBlockingTest {
        val habitId = 51
        val habit = Habit(id = habitId, name = "Meditation", color = Habit.Color.Green)
        val action1 = Action(habit_id = habitId, timestamp = Instant.parse("2020-12-23T10:15:30Z"))
        val action2 = Action(habit_id = habitId, timestamp = Instant.parse("2020-12-23T10:16:30Z"))

        habitDao.insertHabit(habit)
        habitDao.insertAction(action1, action2)

        val actions = habitDao.getActionsForHabit(habitId)
        val expectedActions = listOf(action1.copy(id = 1), action2.copy(id = 2))
        assertEquals(expectedActions, actions)
    }

    @Test
    fun readActionsByHabit() = testCoroutineScope.runBlockingTest {
        val habitId = 875
        val habit1 = Habit(id = habitId, name = "Meditation", color = Habit.Color.Green)
        val habit2 = Habit(name = "Drinking enough water", color = Habit.Color.Green)
        val habit3 = Habit(name = "Workout", color = Habit.Color.Green)
        val action1 = Action(habit_id = habitId, timestamp = Instant.parse("2020-12-23T18:16:30Z"))
        val action2 = Action(habit_id = habitId, timestamp = Instant.parse("2020-12-23T18:16:40Z"))
        val action3 = Action(habit_id = 876, timestamp = Instant.parse("2020-12-23T10:18:42Z"))
        val action4 = Action(habit_id = 877, timestamp = Instant.parse("2020-12-23T10:19:10Z"))

        habitDao.insertHabit(habit1, habit2, habit3)
        habitDao.insertAction(action1, action2, action3, action4)

        val actions = habitDao.getActionsForHabit(habitId)
        val expectedActions = listOf(action1.copy(id = 1), action2.copy(id = 2))
        assertEquals(expectedActions, actions)
    }

    @Test
    fun readActionsAfterTime() = testCoroutineScope.runBlockingTest {
        val action1 = Action(id = 1, habit_id = 1, timestamp = Instant.parse("2020-12-23T18:16:30Z"))
        val action2 = Action(id = 2, habit_id = 1, timestamp = Instant.parse("2020-12-24T18:16:40Z"))
        val action3 = Action(id = 3, habit_id = 1, timestamp = Instant.parse("2020-12-25T10:18:42Z"))
        val action4 = Action(id = 4, habit_id = 1, timestamp = Instant.parse("2020-12-26T10:19:10Z"))

        habitDao.insertAction(action1, action2, action3, action4)

        val actions = habitDao.getActionsAfter(Instant.parse("2020-12-24T20:00:00Z"))
        assertEquals(listOf(action3, action4), actions)
    }

    @Test
    fun readAllHabitsWithActions() = testCoroutineScope.runBlockingTest {
        val habit1 = Habit(id = 1, name = "New habit", color = Habit.Color.Green)
        val habit2 = Habit(id = 2, name = "Other new habit", color = Habit.Color.Green)
        val action1 = Action(id = 1, habit_id = habit1.id, timestamp = Instant.parse("2020-12-23T18:16:30Z"))
        val action2 = Action(id = 2, habit_id = habit2.id, timestamp = Instant.parse("2020-12-24T18:16:40Z"))
        val action3 = Action(id = 3, habit_id = habit2.id, timestamp = Instant.parse("2020-12-25T10:18:42Z"))
        val action4 = Action(id = 4, habit_id = habit1.id, timestamp = Instant.parse("2020-12-26T10:19:10Z"))

        val observer = habitDao.getHabitsWithActions().testObserver()
        habitDao.insertHabit(habit1, habit2)
        habitDao.insertAction(action1, action2, action3, action4)

        val habitsWithActions = observer.observedValues.last()
        val expectedHabitsWithActions = listOf(
            HabitWithActions(habit1, listOf(action1, action4)),
            HabitWithActions(habit2, listOf(action2, action3))
        )
        assertEquals(expectedHabitsWithActions, habitsWithActions)
    }

}