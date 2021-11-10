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
import com.ofalvai.habittracker.ui.model.Habit
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import com.ofalvai.habittracker.persistence.entity.Habit as HabitEntity

class AddHabitViewModel(
    private val dao: HabitDao,
    private val onboardingManager: OnboardingManager
) : ViewModel() {

    private val backNavigationEventChannel = Channel<Unit>(Channel.BUFFERED)
    val backNavigationEvent: Flow<Unit> = backNavigationEventChannel.receiveAsFlow()

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                val habitCount = dao.getTotalHabitCount().first()

                val habitEntity = HabitEntity(
                    name = habit.name,
                    color = habit.color.toEntityColor(),
                    order = habitCount,
                    archived = false
                )
                dao.insertHabit(habitEntity)
                onboardingManager.firstHabitCreated()
                backNavigationEventChannel.send(Unit)
            } catch (e: Throwable) {
                // TODO: error handling on UI
                Timber.e(e)
            }
        }
    }
}