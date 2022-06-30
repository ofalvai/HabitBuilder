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

package com.ofalvai.habittracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

typealias HabitId = Int

@Entity
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: Color,
    val order: Int,
    val archived: Boolean,
    val notes: String
) {

    enum class Color {
        Red,
        Green,
        Blue,
        Yellow,
        Cyan,
        Pink,
    }

}

/**
 * Partial Habit class to allow deleting by ID only
 */
data class HabitById(
    val id: Int
)