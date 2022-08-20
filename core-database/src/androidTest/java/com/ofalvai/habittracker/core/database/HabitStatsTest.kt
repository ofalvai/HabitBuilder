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
import com.ofalvai.habittracker.core.database.entity.*
import com.ofalvai.habittracker.core.testing.BaseInstrumentedTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.Month

@RunWith(AndroidJUnit4::class)
class HabitStatsTest : BaseInstrumentedTest() {

    private object TestData {
        val habit1 = Habit(
            id = 875,
            name = "Meditation",
            color = Habit.Color.Green,
            order = 0,
            archived = false,
            notes = ""
        )
        val habit2 = Habit(
            id = 876,
            name = "Drinking enough water",
            color = Habit.Color.Green,
            order = 1,
            archived = false,
            notes = ""
        )
        val habit3 = Habit(
            id = 877,
            name = "Workout",
            color = Habit.Color.Green,
            order = 2,
            archived = false,
            notes = ""
        )
        val habit4 = Habit(
            id = 878,
            name = "Habit I never do",
            color = Habit.Color.Yellow,
            order = 3,
            archived = false,
            notes = ""
        )

        val actions = listOf(
            Action(
                habit_id = habit1.id,
                timestamp = Instant.parse("2019-12-23T18:16:30Z")
            ),
            Action(
                habit_id = habit1.id,
                timestamp = Instant.parse("2020-12-23T18:16:30Z")
            ),
            Action(
                habit_id = habit1.id,
                timestamp = Instant.parse("2020-12-24T18:16:40Z")
            ),
            Action(
                habit_id = habit2.id,
                timestamp = Instant.parse("2020-12-23T10:18:42Z")
            ),
            Action(
                habit_id = habit3.id,
                timestamp = Instant.parse("2020-12-23T10:19:10Z")
            ),
            Action(
                habit_id = habit1.id,
                timestamp = Instant.parse("2020-12-31T08:59:00Z")
            ),
            Action(
                habit_id = habit1.id,
                timestamp = Instant.parse("2021-01-01T11:56:10Z")
            ),
            Action(
                habit_id = habit1.id,
                timestamp = Instant.parse("2021-01-04T10:28:10Z")
            ),
            Action(
                habit_id = habit1.id,
                timestamp = Instant.parse("2021-03-29T10:28:10Z")
            ),
            Action(
                habit_id = habit1.id,
                timestamp = Instant.parse("2021-08-29T10:28:10Z")
            ),
        )
    }

    private lateinit var habitDao: HabitDao
    private lateinit var db: AppDatabase

    @Before
    fun before() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        habitDao = db.habitDao()

        habitDao.insertHabits(listOf(TestData.habit1, TestData.habit2, TestData.habit3, TestData.habit4))
        habitDao.insertActions(TestData.actions)
    }

    @After
    @Throws(IOException::class)
    fun after() {
        db.close()
    }

    @Test
    fun testActionCountByWeek() = runTest {
        val actionCountsByWeek = habitDao.getActionCountByWeek(TestData.habit1.id)

        val expected = listOf(
            ActionCountByWeek(2019, 52, 1),
            ActionCountByWeek(2020, 52, 2),
            ActionCountByWeek(2020, 53, 2), // W53: 2020-12-31, 2021-01-01
            ActionCountByWeek(2021, 1, 1), // W1: 2021-01-04
            ActionCountByWeek(2021, 13, 1),
            ActionCountByWeek(2021, 34, 1)
        )
        assertEquals(expected, actionCountsByWeek)
    }

    @Test
    fun testEmptyActionCountByWeek() = runTest {
        val actionCountsByWeek = habitDao.getActionCountByWeek(TestData.habit4.id)

        assertEquals(emptyList<ActionCountByWeek>(), actionCountsByWeek)
    }

    @Test
    fun testActionCountByMonth() = runTest {
        val actionCountByMonth = habitDao.getActionCountByMonth(TestData.habit1.id)

        val expected = listOf(
            ActionCountByMonth(2019, Month.of(12), 1),
            ActionCountByMonth(2020, Month.of(12), 3),
            ActionCountByMonth(2021, Month.of(1), 2),
            ActionCountByMonth(2021, Month.of(3), 1),
            ActionCountByMonth(2021, Month.of(8), 1)
        )
        assertEquals(expected, actionCountByMonth)
    }

    @Test
    fun testEmptyActionCountByMonth() = runTest {
        val actionCountByMonth = habitDao.getActionCountByMonth(TestData.habit4.id)

        assertEquals(emptyList<ActionCountByMonth>(), actionCountByMonth)
    }

    @Test
    fun testCompletionRate() = runTest {
        val completionRate = habitDao.getCompletionRate(TestData.habit1.id)

        val expectedCompletion = ActionCompletionRate(
            first_day = Instant.parse("2019-12-23T18:16:30Z"),
            action_count = 8
        )
        val today = LocalDate.of(2021, 3, 29)
        val expectedRate = 0.017278617f
        assertEquals(expectedCompletion, completionRate)
        assertEquals(expectedRate, expectedCompletion.rateAsOf(today))
    }

    @Test
    fun testEmptyCompletionRate() = runTest {
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
    fun testSingleItemCompletionRate() = runTest {
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
    fun testSingleItemCompletionRateOnSameDay() = runTest {
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