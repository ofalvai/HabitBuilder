/*
 * Copyright 2023 Olivér Falvai
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
import com.ofalvai.habittracker.core.database.entity.Action
import com.ofalvai.habittracker.core.database.entity.Habit
import com.ofalvai.habittracker.core.database.entity.HabitDayView
import com.ofalvai.habittracker.core.testing.BaseInstrumentedTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DayViewTest : BaseInstrumentedTest() {

    private object TestData {
        val habit1 = Habit(id = 875, name = "Meditation", color = Habit.Color.Green, order = 0, archived = false, notes = "")
        val habit2 = Habit(id = 876, name = "Drinking enough water", color = Habit.Color.Green, order = 1, archived = false, notes = "")
        val habit3 = Habit(id = 877, name = "Workout", color = Habit.Color.Green, order = 2, archived = false, notes = "")
        val habit4 = Habit(id = 878, name = "Habit I never do", color = Habit.Color.Yellow, order = 3, archived = false, notes = "")
        val habit5 = Habit(id = 879, name = "Habit I do mostly on Friday", color = Habit.Color.Blue, order = 4, archived = false, notes = "")
        val habits = listOf(habit1, habit2, habit3, habit4, habit5)

        val actions = listOf(
            Action(habit_id = habit1.id, timestamp = Instant.parse("2019-12-23T18:16:30Z")),// Mon
            Action(habit_id = habit1.id, timestamp = Instant.parse("2020-12-23T18:16:30Z")),// Wed
            Action(habit_id = habit1.id, timestamp = Instant.parse("2020-12-24T18:16:40Z")),// Thu
            Action(habit_id = habit2.id, timestamp = Instant.parse("2020-12-23T10:18:42Z")),// Wed
            Action(habit_id = habit3.id, timestamp = Instant.parse("2020-12-23T10:19:10Z")),// Wed
            Action(habit_id = habit1.id, timestamp = Instant.parse("2020-12-31T08:59:00Z")),// Thu
            Action(habit_id = habit1.id, timestamp = Instant.parse("2021-01-01T11:56:10Z")),// Fri
            Action(habit_id = habit1.id, timestamp = Instant.parse("2021-01-04T10:28:10Z")),// Mon
            Action(habit_id = habit1.id, timestamp = Instant.parse("2021-03-29T10:28:10Z")),// Mon
            Action(habit_id = habit5.id, timestamp = Instant.parse("2021-03-26T20:00:00Z")),// Fri
            Action(habit_id = habit5.id, timestamp = Instant.parse("2021-03-27T20:00:00Z")),// Sat
            Action(habit_id = habit5.id, timestamp = Instant.parse("2021-03-28T20:00:00Z")),// Sun
            Action(habit_id = habit5.id, timestamp = Instant.parse("2021-04-02T20:00:00Z")),// Fri
            Action(habit_id = habit5.id, timestamp = Instant.parse("2021-04-09T20:00:00Z")),// Fri
            Action(habit_id = habit5.id, timestamp = Instant.parse("2021-04-15T20:00:00Z")),// Thu
            Action(habit_id = habit5.id, timestamp = Instant.parse("2021-04-16T20:00:00Z")),// Fri
        )
    }

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
    fun testHabitsAtEmptyDate() = runTest {
        // Given
        habitDao.insertHabits(TestData.habits)
        habitDao.insertActions(TestData.actions)

        // When
        val habitsAtDate = habitDao.getHabitDayViewsAt(LocalDate.of(2023, 2, 25))

        // Then
        val expected = TestData.habits.map {
            HabitDayView(habit = it, timestamp = null)
        }
        Assert.assertEquals(expected, habitsAtDate)
    }

    @Test
    fun testHabitsAtDate() = runTest {
        // Given
        habitDao.insertHabits(TestData.habits)
        habitDao.insertActions(TestData.actions)

        // When
        val habitsAtDate = habitDao.getHabitDayViewsAt(LocalDate.of(2020, 12, 23))

        // Then
        val expected = listOf(
            HabitDayView(habit = TestData.habit1, timestamp = Instant.parse("2020-12-23T18:16:30Z")),
            HabitDayView(habit = TestData.habit2, timestamp = Instant.parse("2020-12-23T10:18:42Z")),
            HabitDayView(habit = TestData.habit3, timestamp = Instant.parse("2020-12-23T10:19:10Z")),
            HabitDayView(habit = TestData.habit4, timestamp = null),
            HabitDayView(habit = TestData.habit5, timestamp = null)
        )
        Assert.assertEquals(expected, habitsAtDate)
    }
}