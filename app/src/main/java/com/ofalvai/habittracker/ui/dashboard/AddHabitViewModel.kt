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

package com.ofalvai.habittracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.toEntityColor
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.common.SingleLiveEvent
import com.ofalvai.habittracker.ui.model.Habit
import kotlinx.coroutines.launch

class AddHabitViewModel(
    private val dao: HabitDao,
) : ViewModel() {

    val backNavigationEvent = SingleLiveEvent<Void>()

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            val habitEntity = com.ofalvai.habittracker.persistence.entity.Habit(
                name = habit.name,
                color = habit.color.toEntityColor()
            )
            dao.insertHabit(habitEntity)
            backNavigationEvent.call()
        }
    }
}