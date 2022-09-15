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

package com.ofalvai.habittracker.feature.misc

import com.ofalvai.habittracker.core.database.entity.Action
import com.ofalvai.habittracker.core.database.entity.Habit
import com.ofalvai.habittracker.feature.misc.export.CSVHandler
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.StringReader
import java.time.Instant

class CSVHandlerTest {

    @Test
    fun `Habit list export test`() {
        // Given
        val habits = listOf(
            Habit(1, "Meditation", Habit.Color.Blue, 0, false, ""),
            Habit(2, "Meditation", Habit.Color.Yellow, 1, false, "Notes notes notes"),
            Habit(3, "Meditation", Habit.Color.Red, 3, true, "Multi-line \n description")
        )

        // When
        val csv = CSVHandler.exportHabitList(habits).toString()

        // Then
        val expectedCSV = """id,name,color,order,archived,notes
1,Meditation,Blue,0,false,
2,Meditation,Yellow,1,false,Notes notes notes
3,Meditation,Red,3,true,"Multi-line 
 description"
"""
        assertEquals(expectedCSV, csv)
    }

    @Test
    fun `Empty habit list export test`() {
        // Given
        val habits = emptyList<Habit>()

        // When
        val csv = CSVHandler.exportHabitList(habits).toString()

        // Then
        val expectedCSV = """id,name,color,order,archived,notes
"""
        assertEquals(expectedCSV, csv)
    }

    @Test
    fun `Action list export test`() {
        // Given
        val actions = listOf(
            Action(1, 1, Instant.ofEpochSecond(1656878248)),
            Action(2, 1, Instant.ofEpochSecond(1656878248)),
            Action(3, 2, Instant.ofEpochSecond(0)),
        )

        // When
        val csv = CSVHandler.exportActionList(actions).toString()

        // Then
        val expectedCSV = """id,habit_id,timestamp
1,1,1656878248000
2,1,1656878248000
3,2,0
"""
        assertEquals(expectedCSV, csv)
    }

    @Test
    fun `Empty action list export test`() {
        // Given
        val actions = emptyList<Action>()

        // When
        val csv = CSVHandler.exportActionList(actions).toString()

        // Then
        val expectedCSV = """id,habit_id,timestamp
"""
        assertEquals(expectedCSV, csv)
    }

    @Test
    fun `Habit list import test`() {
        // Given
        val csv = """id,name,color,order,archived,notes

1,Meditation,Blue,0,false,

2,Meditation,Yellow,1,false,Notes notes notes

3,Meditation,Red,3,true,"Multi-line 
 description"

"""
        // When
        val habitList = CSVHandler.importHabitList(StringReader(csv))

        // Then
        val expectedHabitList = listOf(
            Habit(1, "Meditation", Habit.Color.Blue, 0, false, ""),
            Habit(2, "Meditation", Habit.Color.Yellow, 1, false, "Notes notes notes"),
            Habit(3, "Meditation", Habit.Color.Red, 3, true, "Multi-line \n description")
        )
        assertEquals(expectedHabitList, habitList)
    }

    @Test
    fun `Empty habit list import test`() {
        // Given
        val csv = """id,name,color,order,archived,notes

"""
        // When
        val habitList = CSVHandler.importHabitList(StringReader(csv))

        // Then
        assertEquals(emptyList<Habit>(), habitList)
    }

    @Test
    fun `Action list import test`() {
        // Given
        val csv = """id,habit_id,timestamp

1,1,1656878248000

2,1,1656878248000

3,2,0

"""
        // When
        val actionList = CSVHandler.importActionList(StringReader(csv))

        // Then
        val expectedActionList = listOf(
            Action(1, 1, Instant.ofEpochSecond(1656878248)),
            Action(2, 1, Instant.ofEpochSecond(1656878248)),
            Action(3, 2, Instant.ofEpochSecond(0)),
        )
        assertEquals(expectedActionList, actionList)
    }

    @Test
    fun `Empty action list import test`() {
        // Given
        val csv = """id,habit_id,timestamp

"""
        // When
        val actionList = CSVHandler.importActionList(StringReader(csv))

        // Then
        assertEquals(emptyList<Action>(), actionList)
    }
}