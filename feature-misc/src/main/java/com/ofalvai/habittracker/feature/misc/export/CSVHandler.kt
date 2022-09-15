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

package com.ofalvai.habittracker.feature.misc.export

import com.ofalvai.habittracker.core.database.entity.Action
import com.ofalvai.habittracker.core.database.entity.Habit
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.Reader
import java.time.Instant

private val FORMAT = CSVFormat.DEFAULT
    .builder()
    .setRecordSeparator("\n")
    .build()

internal class BackupData(
    val habits: List<Habit>,
    val actions: List<Action>,
    val backupVersion: Int
)

internal object CSVHandler {

    fun exportHabitList(habits: List<Habit>): StringBuilder {
        val stringBuilder = StringBuilder()
        val printer = CSVPrinter(stringBuilder, FORMAT)
        printer.printRecord("id", "name", "color", "order", "archived", "notes")

        habits.forEach {
            printer.printRecord(it.id, it.name, it.color, it.order, it.archived, it.notes)
        }

        return stringBuilder
    }

    fun exportActionList(actions: List<Action>): StringBuilder {
        val stringBuilder = StringBuilder()
        val printer = CSVPrinter(stringBuilder, FORMAT)
        printer.printRecord("id", "habit_id", "timestamp")

        actions.forEach {
            printer.printRecord(it.id, it.habit_id, it.timestamp.toEpochMilli())
        }

        return stringBuilder
    }

    fun importHabitList(csvReader: Reader): List<Habit> {
        val habits = FORMAT.builder().setHeader().build().parse(csvReader).map {
            Habit(
                id = it.get("id").toInt(),
                name = it.get("name"),
                color = Habit.Color.valueOf(it.get("color")),
                order = it.get("order").toInt(),
                archived = it.get("archived").toBoolean(),
                notes = it.get("notes")
            )
        }
        return habits
    }

    fun importActionList(csvReader: Reader): List<Action> {
        val actions = FORMAT.builder().setHeader().build().parse(csvReader).map {
            Action(
                id = it.get("id").toInt(),
                habit_id = it.get("habit_id").toInt(),
                timestamp = Instant.ofEpochMilli(it.get("timestamp").toLong())
            )
        }
        return actions
    }
}