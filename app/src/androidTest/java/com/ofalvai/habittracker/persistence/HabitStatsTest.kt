/*
 * Copyright 2021 Olivér Falvai
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

package com.ofalvai.habittracker.persistence

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ofalvai.habittracker.persistence.entity.*
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
import java.time.LocalDate

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class HabitStatsTest {

    private object TestData {
        val habit1 = Habit(id = 875, name = "Meditation", color = Habit.Color.Green)
        val habit2 = Habit(id = 876, name = "Drinking enough water", color = Habit.Color.Green)
        val habit3 = Habit(id = 877, name = "Workout", color = Habit.Color.Green)
        val habit4 = Habit(id = 878, name = "Habit I never do", color = Habit.Color.Yellow)

        val actions = arrayOf(
            Action(habit_id = habit1.id, timestamp = Instant.parse("2019-12-23T18:16:30Z")),
            Action(habit_id = habit1.id, timestamp = Instant.parse("2020-12-23T18:16:30Z")),
            Action(habit_id = habit1.id, timestamp = Instant.parse("2020-12-24T18:16:40Z")),
            Action(habit_id = habit2.id, timestamp = Instant.parse("2020-12-23T10:18:42Z")),
            Action(habit_id = habit3.id, timestamp = Instant.parse("2020-12-23T10:19:10Z")),
            Action(habit_id = habit1.id, timestamp = Instant.parse("2020-12-31T08:59:00Z")),
            Action(habit_id = habit1.id, timestamp = Instant.parse("2021-01-01T11:56:10Z")),
            Action(habit_id = habit1.id, timestamp = Instant.parse("2021-01-04T10:28:10Z")),
            Action(habit_id = habit1.id, timestamp = Instant.parse("2021-03-29T10:28:10Z")),
        )
    }

    private lateinit var habitDao: HabitDao
    private lateinit var db: AppDatabase
    private val testCoroutineScope = TestCoroutineScope()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() = testCoroutineScope.runBlockingTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        habitDao = db.habitDao()

        habitDao.insertHabit(TestData.habit1, TestData.habit2, TestData.habit3, TestData.habit4)
        habitDao.insertAction(*TestData.actions)
    }

    @After
    @Throws(IOException::class)
    fun after() {
        db.close()
    }

    @Test
    fun testActionCountByWeek() = testCoroutineScope.runBlockingTest {
        val actionCountsByWeek = habitDao.getActionCountByWeek(TestData.habit1.id)

        val expected = listOf(
            ActionCountByWeek(2019, 52, 1),
            ActionCountByWeek(2020, 52, 2),
            ActionCountByWeek(2020, 53, 2), // W53: 2020-12-31, 2021-01-01
            ActionCountByWeek(2021, 1, 1), // W1: 2021-01-04
            ActionCountByWeek(2021, 13, 1)
        )
        assertEquals(expected, actionCountsByWeek)
    }

    @Test
    fun testEmptyActionCountByWeek() = testCoroutineScope.runBlockingTest {
        val actionCountsByWeek = habitDao.getActionCountByWeek(TestData.habit4.id)

        assertEquals(emptyList<ActionCountByWeek>(), actionCountsByWeek)
    }

    @Test
    fun testActionCountByMonth() = testCoroutineScope.runBlockingTest {
        val actionCountByMonth = habitDao.getActionCountByMonth(TestData.habit1.id)

        val expected = listOf(
            ActionCountByMonth(2019, 12, 1),
            ActionCountByMonth(2020, 12, 3),
            ActionCountByMonth(2021, 1, 2),
            ActionCountByMonth(2021, 3, 1)
        )
        assertEquals(expected, actionCountByMonth)
    }

    @Test
    fun testEmptyActionCountByMonth() = testCoroutineScope.runBlockingTest {
        val actionCountByMonth = habitDao.getActionCountByMonth(TestData.habit4.id)

        assertEquals(emptyList<ActionCountByMonth>(), actionCountByMonth)
    }

    @Test
    fun testCompletionRate() = testCoroutineScope.runBlockingTest {
        val completionRate = habitDao.getCompletionRate(TestData.habit1.id)

        val expectedCompletion = ActionCompletionRate(
            first_day = Instant.parse("2019-12-23T18:16:30Z"),
            action_count = 7
        )
        val today = LocalDate.of(2021, 3, 29)
        val expectedRate = 0.015118791f
        assertEquals(expectedCompletion, completionRate)
        assertEquals(expectedRate, expectedCompletion.rateAsOf(today))
    }

    @Test
    fun testEmptyCompletionRate() = testCoroutineScope.runBlockingTest {
        val completionRate = habitDao.getCompletionRate(TestData.habit4.id)

        val expectedCompletion = ActionCompletionRate(
            first_day = Instant.EPOCH,
            action_count = 0
        )
        val today = LocalDate.of(2021, 3, 29)
        val expectedRate = 0f
        assertEquals(expectedCompletion, completionRate)
        assertEquals(expectedRate, expectedCompletion.rateAsOf(today))
    }

    @Test
    fun testSingleItemCompletionRate() = testCoroutineScope.runBlockingTest {
        val completionRate = habitDao.getCompletionRate(TestData.habit2.id)

        val expectedCompletion = ActionCompletionRate(
            first_day = Instant.parse("2020-12-23T10:18:42Z"),
            action_count = 1
        )
        assertEquals(expectedCompletion, completionRate)
        val today = LocalDate.of(2021, 3, 29)
        val expectedRate = 0.010309278f
        assertEquals(expectedRate, expectedCompletion.rateAsOf(today))
    }

    @Test
    fun testSingleItemCompletionRateOnSameDay() = testCoroutineScope.runBlockingTest {
        val completionRate = habitDao.getCompletionRate(TestData.habit2.id)

        val expectedCompletion = ActionCompletionRate(
            first_day = Instant.parse("2020-12-23T10:18:42Z"),
            action_count = 1
        )
        assertEquals(expectedCompletion, completionRate)
        val today = LocalDate.of(2020, 12, 23)
        val expectedRate = 1f
        assertEquals(expectedRate, expectedCompletion.rateAsOf(today))
    }

}