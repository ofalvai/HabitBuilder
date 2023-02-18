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

package com.ofalvai.habittracker.feature.dashboard.ui.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.core.common.OnboardingManager
import com.ofalvai.habittracker.core.common.Telemetry
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.feature.dashboard.mapper.toEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val dao: HabitDao,
    private val onboardingManager: OnboardingManager,
    private val telemetry: Telemetry
) : ViewModel() {

    private val backNavigationEventChannel = Channel<Unit>(Channel.BUFFERED)
    val backNavigationEvent: Flow<Unit> = backNavigationEventChannel.receiveAsFlow()

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                val habitCount = dao.getTotalHabitCount().first()
                val habitEntity = habit.toEntity(order = habitCount, archived = false)
                dao.insertHabits(listOf(habitEntity))
                onboardingManager.firstHabitCreated()
                backNavigationEventChannel.send(Unit)
            } catch (e: Throwable) {
                // TODO: error handling on UI
                telemetry.logNonFatal(e)
            }
        }
    }
}