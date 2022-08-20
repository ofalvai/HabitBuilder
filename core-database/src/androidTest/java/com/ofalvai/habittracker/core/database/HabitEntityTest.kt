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

package com.ofalvai.habittracker.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.ofalvai.habittracker.core.database.entity.Action
import com.ofalvai.habittracker.core.database.entity.Habit
import com.ofalvai.habittracker.core.database.entity.HabitById
import com.ofalvai.habittracker.core.database.entity.HabitWithActions
import com.ofalvai.habittracker.core.testing.BaseInstrumentedTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class HabitEntityTest : BaseInstrumentedTest() {

    private lateinit var habitDao: HabitDao
    private lateinit var db: AppDatabase

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
    fun insertAndReadHabits() = runTest {
        val newHabit1 = Habit(
            name = "New habit",
            color = Habit.Color.Green,
            order = 0,
            archived = false,
            notes = "Habit notes"
        )
        val newHabit2 = Habit(
            name = "Other new habit",
            color = Habit.Color.Green,
            order = 1,
            archived = false,
            notes = ""
        )

        habitDao.insertHabits(listOf(newHabit1, newHabit2))

        val habits = habitDao.getHabits()
        val expected = listOf(newHabit1.copy(id = 1), newHabit2.copy(id = 2))
        assertEquals(expected, habits)
    }

    @Test
    fun insertAndReadActions() = runTest {
        val habitId = 51
        val habit = Habit(
            id = habitId,
            name = "Meditation",
            color = Habit.Color.Green,
            order = 0,
            archived = false,
            ""
        )
        val action1 = Action(
            habit_id = habitId,
            timestamp = Instant.parse("2020-12-23T10:15:30Z")
        )
        val action2 = Action(
            habit_id = habitId,
            timestamp = Instant.parse("2020-12-23T10:16:30Z")
        )

        habitDao.insertHabits(listOf(habit))
        habitDao.insertActions(listOf(action1, action2))

        val actions = habitDao.getActionsForHabit(habitId)
        val expectedActions = listOf(action1.copy(id = 1), action2.copy(id = 2))
        assertEquals(expectedActions, actions)
    }

    @Test
    fun readActionsByHabit() = runTest {
        val habitId = 875
        val habit1 = Habit(
            id = habitId,
            name = "Meditation",
            color = Habit.Color.Green,
            order = 0,
            archived = false,
            notes = ""
        )
        val habit2 = Habit(
            name = "Drinking enough water",
            color = Habit.Color.Green,
            order = 1,
            archived = false,
            notes = ""
        )
        val habit3 = Habit(
            name = "Workout",
            color = Habit.Color.Green,
            order = 2,
            archived = false,
            notes = ""
        )
        val action1 = Action(
            habit_id = habitId,
            timestamp = Instant.parse("2020-12-23T18:16:30Z")
        )
        val action2 = Action(
            habit_id = habitId,
            timestamp = Instant.parse("2020-12-23T18:16:40Z")
        )
        val action3 = Action(
            habit_id = 876,
            timestamp = Instant.parse("2020-12-23T10:18:42Z")
        )
        val action4 = Action(
            habit_id = 877,
            timestamp = Instant.parse("2020-12-23T10:19:10Z")
        )

        habitDao.insertHabits(listOf(habit1, habit2, habit3))
        habitDao.insertActions(listOf(action1, action2, action3, action4))

        val actions = habitDao.getActionsForHabit(habitId)
        val expectedActions = listOf(action1.copy(id = 1), action2.copy(id = 2))
        assertEquals(expectedActions, actions)
    }

    @Test
    fun readActionsAfterTime() = runTest {
        val habit = Habit(
            id = 1,
            name = "New habit",
            color = Habit.Color.Green,
            order = 0,
            archived = false,
            notes = ""
        )
        val action1 = Action(
            id = 1,
            habit_id = 1,
            timestamp = Instant.parse("2020-12-23T18:16:30Z")
        )
        val action2 = Action(
            id = 2,
            habit_id = 1,
            timestamp = Instant.parse("2020-12-24T18:16:40Z")
        )
        val action3 = Action(
            id = 3,
            habit_id = 1,
            timestamp = Instant.parse("2020-12-25T10:18:42Z")
        )
        val action4 = Action(
            id = 4,
            habit_id = 1,
            timestamp = Instant.parse("2020-12-26T10:19:10Z")
        )

        habitDao.insertHabits(listOf(habit))
        habitDao.insertActions(listOf(action1, action2, action3, action4))

        val actions = habitDao.getActionsAfter(Instant.parse("2020-12-24T20:00:00Z"))
        assertEquals(listOf(action3, action4), actions)
    }

    @Test
    fun readAllHabitsWithActions() = runTest {
        val habit1 = Habit(
            id = 1,
            name = "New habit",
            color = Habit.Color.Green,
            order = 0,
            archived = false,
            notes = ""
        )
        val habit2 = Habit(
            id = 2,
            name = "Other new habit",
            color = Habit.Color.Green,
            order = 1,
            archived = false,
            notes = ""
        )
        val habit3 = Habit(
            id = 3,
            name = "Archived habit",
            color = Habit.Color.Yellow,
            order = 2,
            archived = true,
            notes = ""
        )
        val action1 = Action(
            id = 1,
            habit_id = habit1.id,
            timestamp = Instant.parse("2020-12-23T18:16:30Z")
        )
        val action2 = Action(
            id = 2,
            habit_id = habit2.id,
            timestamp = Instant.parse("2020-12-24T18:16:40Z")
        )
        val action3 = Action(
            id = 3,
            habit_id = habit2.id,
            timestamp = Instant.parse("2020-12-25T10:18:42Z")
        )
        val action4 = Action(
            id = 4,
            habit_id = habit1.id,
            timestamp = Instant.parse("2020-12-26T10:19:10Z")
        )

        habitDao.insertHabits(listOf(habit1, habit2, habit3))
        habitDao.insertActions(listOf(action1, action2, action3, action4))

        habitDao.getActiveHabitsWithActions().test {
            val expectedHabitsWithActions = listOf(
                HabitWithActions(habit1, listOf(action1, action4)),
                HabitWithActions(habit2, listOf(action2, action3))
            )
            assertEquals(expectedHabitsWithActions, awaitItem())
        }
    }

    @Test
    fun deleteHabitWithActions() = runTest {
        val habitId = 51
        val habit = Habit(
            id = habitId,
            name = "Meditation",
            color = Habit.Color.Green,
            order = 0,
            archived = false,
            notes = ""
        )
        val action1 = Action(
            habit_id = habitId,
            timestamp = Instant.parse("2021-05-31T10:15:30Z")
        )
        val action2 = Action(
            habit_id = habitId,
            timestamp = Instant.parse("2021-05-31T10:16:30Z")
        )

        habitDao.insertHabits(listOf(habit))
        habitDao.insertActions(listOf(action1, action2))
        habitDao.deleteHabit(HabitById(habit.id))

        val actions = habitDao.getActionsForHabit(habitId)
        val expectedActions = emptyList<Action>()
        assertEquals(expectedActions, actions)

        val habits = habitDao.getHabits()
        val expectedHabits = emptyList<Habit>()
        assertEquals(expectedHabits, habits)
    }

    @Test
    fun readHabitsWithModifiedOrders() = runTest {
        val newHabit1 = Habit(
            name = "New habit",
            color = Habit.Color.Green,
            order = 5,
            archived = false,
            notes = ""
        )
        val newHabit2 = Habit(
            name = "Other new habit",
            color = Habit.Color.Green,
            order = 1,
            archived = false,
            notes = ""
        )
        val newHabit3 = Habit(
            name = "Meditation",
            color = Habit.Color.Green,
            order = 2,
            archived = false,
            notes = ""
        )
        val newHabit4 = Habit(
            name = "The first one",
            color = Habit.Color.Yellow,
            order = 0,
            archived = false,
            notes = ""
        )

        habitDao.insertHabits(listOf(newHabit1, newHabit2, newHabit3, newHabit4))

        val habits = habitDao.getHabits()
        val expected = listOf(
            newHabit4.copy(id = 4),
            newHabit2.copy(id = 2),
            newHabit3.copy(id = 3),
            newHabit1.copy(id = 1)
        )
        assertEquals(expected, habits)
    }

    @Test
    fun swapHabitOrders() = runTest {
        val newHabit1 = Habit(
            id = 1,
            name = "New habit",
            color = Habit.Color.Green,
            order = 5,
            archived = false,
            notes = ""
        )
        val newHabit2 = Habit(
            id = 2,
            name = "Other new habit",
            color = Habit.Color.Green,
            order = 1,
            archived = false,
            notes = ""
        )
        val newHabit3 = Habit(
            id = 3,
            name = "Meditation",
            color = Habit.Color.Green,
            order = 2,
            archived = false,
            notes = ""
        )
        val newHabit4 = Habit(
            id = 4,
            name = "The first one",
            color = Habit.Color.Yellow,
            order = 0,
            archived = false,
            notes = ""
        )

        habitDao.insertHabits(listOf(newHabit1, newHabit2, newHabit3, newHabit4))

        val habitPair = habitDao.getHabitPair(newHabit3.id, newHabit4.id)
        habitDao.updateHabitOrders(
            id1 = newHabit3.id,
            order1 = habitPair[1].order,
            id2 = newHabit4.id,
            order2 = habitPair[0].order
        )

        val habits = habitDao.getHabits()
        val expected = listOf(
            newHabit3.copy(order = 0),
            newHabit2,
            newHabit4.copy(order = 2),
            newHabit1,
        )
        assertEquals(expected, habits)
    }

}