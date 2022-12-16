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

import com.ofalvai.habittracker.core.database.entity.ActionCompletionRate
import com.ofalvai.habittracker.core.database.entity.ActionCountByWeek
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.ActionHistory
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.feature.dashboard.mapper.mapHabitEntityToModel
import com.ofalvai.habittracker.feature.dashboard.mapper.mapHabitSingleStats
import com.ofalvai.habittracker.feature.dashboard.ui.model.SingleStats
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import com.ofalvai.habittracker.core.database.entity.Action as ActionEntity
import com.ofalvai.habittracker.core.database.entity.Habit as HabitEntity
import com.ofalvai.habittracker.core.database.entity.HabitWithActions as HabitWithActionsEntity

class HabitMapperTest {

    @Test
    fun `Given habit with actions in the last 30 days When mapped to model Then history contains the toggled actions`()  {
        // Given
        val now = Instant.now()
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = now),
            ActionEntity(id = 2, habit_id = 0, timestamp = now.minus(1, ChronoUnit.DAYS)),
            ActionEntity(id = 3, habit_id = 0, timestamp = now.minus(3, ChronoUnit.DAYS))
        )
        val habits = listOf(givenHabitWithActions(actions))

        // When
        val mappedHabitsWithActions = mapHabitEntityToModel(habits)

        // Then
        val expectedActionHistory = (1..23).map { Action(id = 0, toggled = false, timestamp = null) } + listOf(
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(3, true, now.minus(3, ChronoUnit.DAYS)),
            Action(0, false, null),
            Action(2, true, now.minus(1, ChronoUnit.DAYS)),
            Action(1, true, now)
        )
        val expectedHabits = listOf(
            HabitWithActions(
                Habit(
                    0,
                    "Meditation",
                    Habit.Color.Red,
                    "Doing right after waking up in the living room"
                ),
                expectedActionHistory.toImmutableList(),
                3,
                ActionHistory.Streak(2)
            )
        )
        assertEquals(expectedHabits, mappedHabitsWithActions)
    }

    @Test
    fun `Given habit with actions before the last 30 days When mapped to model Then last 30 days' actions are empty`() {
        // Given
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = Instant.now().minus(40, ChronoUnit.DAYS)),
            ActionEntity(id = 2, habit_id = 0, timestamp = Instant.now().minus(49, ChronoUnit.DAYS))
        )
        val habits = listOf(givenHabitWithActions(actions))

        // When
        val mappedHabitsWithActions = mapHabitEntityToModel(habits)

        // Then
        val expectedActionHistory = (1..30).map { Action(0, false, null) }
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Red, "Doing right after waking up in the living room"),
            expectedActionHistory.toImmutableList(),
            2,
            ActionHistory.MissedDays(40)
        ))
        assertEquals(expectedHabits, mappedHabitsWithActions)
    }

    @Test
    fun `Given habit with actions in last 30 days and before as well When mapped to model Then history contains actions only from last 30 days`() {
        // Given
        val now = Instant.now()
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = now),
            ActionEntity(id = 2, habit_id = 0, timestamp = now.minus(3, ChronoUnit.DAYS)),
            ActionEntity(id = 3, habit_id = 0, timestamp = now.minus(1, ChronoUnit.DAYS)),
            ActionEntity(id = 4, habit_id = 0, timestamp = now.minus(5, ChronoUnit.DAYS)),
            ActionEntity(id = 6, habit_id = 0, timestamp = now.minus(30, ChronoUnit.DAYS)),
            ActionEntity(id = 5, habit_id = 0, timestamp = now.minus(54, ChronoUnit.DAYS)),
        )
        val habits = listOf(givenHabitWithActions(actions))

        // When
        val mappedHabitsWithActions = mapHabitEntityToModel(habits)

        // Then
        val expectedActionHistory = (1..23).map { Action(id = 0, toggled = false, timestamp = null) } + listOf(
            Action(0, false, null),
            Action(4, true, now.minus(5, ChronoUnit.DAYS)),
            Action(0, false, null),
            Action(2, true, now.minus(3, ChronoUnit.DAYS)),
            Action(0, false, null),
            Action(3, true, now.minus(1, ChronoUnit.DAYS)),
            Action(1, true, now)
        )
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Red, "Doing right after waking up in the living room"),
            expectedActionHistory.toImmutableList(),
            6,
            ActionHistory.Streak(2)
        ))
        assertEquals(expectedHabits, mappedHabitsWithActions)
    }

    @Test
    fun `Given actions in the last 4 days When mapped to model Then action history is 4-day streak`() {
        // Given
        val now = Instant.now()
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = now),
            ActionEntity(id = 2, habit_id = 0, timestamp = now.minus(1, ChronoUnit.DAYS)),
            ActionEntity(id = 3, habit_id = 0, timestamp = now.minus(2, ChronoUnit.DAYS)),
            ActionEntity(id = 4, habit_id = 0, timestamp = now.minus(3, ChronoUnit.DAYS)),
        )
        val habits = listOf(givenHabitWithActions(actions))

        // When
        val mappedHabitsWithActions = mapHabitEntityToModel(habits)

        // Then
        val expectedActionHistory = (1..23).map { Action(id = 0, toggled = false, timestamp = null) } + listOf(
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(4, true, now.minus(3, ChronoUnit.DAYS)),
            Action(3, true, now.minus(2, ChronoUnit.DAYS)),
            Action(2, true, now.minus(1, ChronoUnit.DAYS)),
            Action(1, true, now)
        )
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Red, "Doing right after waking up in the living room"),
            expectedActionHistory.toImmutableList(),
            4,
            ActionHistory.Streak(4)
        ))
        assertEquals(expectedHabits, mappedHabitsWithActions)
    }

    @Test
    fun `Given habit with no actions When mapped to model Then action history is clean`() {
        // Given
        val habits = listOf(givenHabitWithActions(emptyList()))

        // When
        val mappedHabitsWithActions = mapHabitEntityToModel(habits)

        // Then
        val expectedActionHistory = (1..30).map { Action(0, false, null) }
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Red, "Doing right after waking up in the living room"),
            expectedActionHistory.toImmutableList(),
            0,
            ActionHistory.Clean
        ))
        assertEquals(expectedHabits, mappedHabitsWithActions)
    }

    @Test
    fun `Given habit with last action 3 days ago When mapped to model Then action history is 3-day missed streak`() {
        // Given
        val now = Instant.now()
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = now.minus(3, ChronoUnit.DAYS)),
        )
        val habits = listOf(givenHabitWithActions(actions))

        // When
        val mappedHabitsWithActions = mapHabitEntityToModel(habits)

        // Then
        val expectedActionHistory = (1..23).map { Action(id = 0, toggled = false, timestamp = null) } + listOf(
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(1, true, now.minus(3, ChronoUnit.DAYS)),
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null)
        )
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Red, "Doing right after waking up in the living room"),
            expectedActionHistory.toImmutableList(),
            1,
            ActionHistory.MissedDays(3)
        ))
        assertEquals(expectedHabits, mappedHabitsWithActions)
    }

    @Test
    fun `Given habit with today as only action day When mapped to model Then action history is 1-day streak`() {
        // Given
        val now = Instant.now()
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = now),
        )
        val habits = listOf(givenHabitWithActions(actions))

        // When
        val mappedHabitsWithActions = mapHabitEntityToModel(habits)

        // Then
        val expectedActionHistory = (1..23).map { Action(id = 0, toggled = false, timestamp = null) } + listOf(
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(1, true, now)
        )
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Red, "Doing right after waking up in the living room"),
            expectedActionHistory.toImmutableList(),
            1,
            ActionHistory.Streak(1)
        ))
        assertEquals(expectedHabits, mappedHabitsWithActions)
    }

    @Test
    fun `Given habit with yesterday as only action day When mapped to model Then action history is 1-day missed streak`() {
        // Given
        val now = Instant.now()
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = now.minus(1, ChronoUnit.DAYS)),
        )
        val habits = listOf(givenHabitWithActions(actions))

        // When
        val mappedHabitsWithActions = mapHabitEntityToModel(habits)

        // Then
        val expectedActionHistory = (1..23).map { Action(id = 0, toggled = false, timestamp = null) } + listOf(
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(0, false, null),
            Action(1, true, now.minus(1, ChronoUnit.DAYS)),
            Action(0, false, null)
        )
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Red, "Doing right after waking up in the living room"),
            expectedActionHistory.toImmutableList(),
            1,
            ActionHistory.MissedDays(1)
        ))
        assertEquals(expectedHabits, mappedHabitsWithActions)
    }

    @Test
    fun `Given habit with action in the future When mapped to model Then nothing weird happens`() {
        // Given
        val now = Instant.now()
        val actions = listOf(
            ActionEntity(id = 1, habit_id = 0, timestamp = now.plus(1, ChronoUnit.DAYS)),
        )
        val habits = listOf(givenHabitWithActions(actions))

        // When
        val mappedHabitsWithActions = mapHabitEntityToModel(habits)

        // Then
        val expectedActionHistory = (1..30).map { Action(0, false, null) }
        val expectedHabits = listOf(HabitWithActions(
            Habit(0, "Meditation", Habit.Color.Red, "Doing right after waking up in the living room"),
            expectedActionHistory.toImmutableList(),
            1,
            ActionHistory.Clean
        ))
        assertEquals(expectedHabits, mappedHabitsWithActions)
    }

    @Test
    fun `Given habit with no actions When single stats are mapped Then stats are empty`() {
        // Given
        val now = LocalDate.now()
        val completionRate = ActionCompletionRate(
            first_day = Instant.EPOCH,
            action_count = 0
        )
        val weeklyActionCountList = emptyList<ActionCountByWeek>()

        // When
        val singleStats = mapHabitSingleStats(completionRate, weeklyActionCountList, now)

        // Then
        val expected = SingleStats(
            firstDay = null,
            actionCount = 0,
            weeklyActionCount = 0,
            completionRate = 0f
        )
        assertEquals(expected, singleStats)
    }

    @Test
    fun `Given habit with single action today When single stats are mapped Then stats are correct`() {
        // Given
        val now = LocalDate.of(2021, 5, 24)
        val completionRate = ActionCompletionRate(
            first_day = now.atTime(9, 24).toInstant(OffsetDateTime.now().offset),
            action_count = 1
        )
        val weeklyActionCountList = persistentListOf(
            ActionCountByWeek(
                year = 2021,
                week = 21,
                1
            )
        )

        // When
        val singleStats = mapHabitSingleStats(completionRate, weeklyActionCountList, now)

        // Then
        val expected = SingleStats(
            firstDay = LocalDate.of(2021, 5, 24),
            actionCount = 1,
            weeklyActionCount = 1,
            completionRate = 1f
        )
        assertEquals(expected, singleStats)
    }

    @Test
    fun `Given habit with actions last week and this week When single stats are mapped Then stats are correct`() {
        // Given
        val now = LocalDate.of(2021, 5, 24)
        val completionRate = ActionCompletionRate(
            first_day = LocalDateTime
                .of(2021, 5, 18, 9, 24)
                .toInstant(OffsetDateTime.now().offset),
            action_count = 5
        )
        val weeklyActionCountList = persistentListOf(
            ActionCountByWeek(
                year = 2021,
                week = 20,
                action_count = 4
            ),
            ActionCountByWeek(
                year = 2021,
                week = 21,
                action_count = 1
            )
        )

        // When
        val singleStats = mapHabitSingleStats(completionRate, weeklyActionCountList, now)

        // Then
        val expected = SingleStats(
            firstDay = LocalDate.of(2021, 5, 18),
            actionCount = 5,
            weeklyActionCount = 1,
            completionRate = 0.7142857143f
        )
        assertEquals(expected, singleStats)
    }

    @Test
    fun `Given habit with actions last week When single stats are mapped Then stats are correct`() {
        // Given
        val now = LocalDate.of(2021, 5, 24)
        val completionRate = ActionCompletionRate(
            first_day = LocalDateTime
                .of(2021, 5, 18, 9, 24)
                .toInstant(OffsetDateTime.now().offset),
            action_count = 4
        )
        val weeklyActionCountList = persistentListOf(
            ActionCountByWeek(
                year = 2021,
                week = 20,
                action_count = 4
            )
        )

        // When
        val singleStats = mapHabitSingleStats(completionRate, weeklyActionCountList, now)

        // Then
        val expected = SingleStats(
            firstDay = LocalDate.of(2021, 5, 18),
            actionCount = 4,
            weeklyActionCount = 0,
            completionRate = 0.5714285714f
        )
        assertEquals(expected, singleStats)
    }

    private fun givenHabitEntity() = HabitEntity(0, "Meditation", HabitEntity.Color.Red, 0, false, "Doing right after waking up in the living room")

    private fun givenHabitWithActions(actions: List<ActionEntity>) = HabitWithActionsEntity(
        habit = givenHabitEntity(),
        actions = actions
    )

}