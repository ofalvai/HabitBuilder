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

package com.ofalvai.habittracker.ui.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.*
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.common.SingleLiveEvent
import com.ofalvai.habittracker.ui.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.*

private val initialSingleStats = SingleStats(null, 0, 0, 0f)

class HabitDetailViewModel(
    private val dao: HabitDao
) : ViewModel() {

    val habitWithActions = MutableStateFlow<Result<HabitWithActions>>(Result.Loading)
    val singleStats = MutableStateFlow(initialSingleStats)
    val actionCountByWeek = MutableStateFlow<List<ActionCountByWeek>>(emptyList())
    val actionCountByMonth = MutableStateFlow<List<ActionCountByMonth>>(emptyList())
    val backNavigationEvent = SingleLiveEvent<Void>()

    fun fetchHabitDetails(habitId: Int): Job {
        return viewModelScope.launch {
            try {
                val habit = dao.getHabitWithActions(habitId).let {
                    // TODO: unify this with the regular mapping (where empty day action are filled)
                    HabitWithActions(
                        Habit(it.habit.id, it.habit.name, it.habit.color.toUIColor()),
                        it.actions.map { action ->
                            Action(action.id, toggled = true, timestamp = action.timestamp)
                        },
                        it.actions.size,
                        actionsToHistory(it.actions)
                    )
                }
                habitWithActions.value = Result.Success(habit)
            } catch (e: Throwable) {
                Timber.e(e)
                habitWithActions.value = Result.Failure(e)
            }
        }
    }

    fun fetchHabitStats(habitId: Int): Job {
        return viewModelScope.launch {
            try {
                val completionRate = async { dao.getCompletionRate(habitId) }
                val actionCountByWeekEntity = async { dao.getActionCountByWeek(habitId) }
                val actionCountByMonthEntity = async { dao.getActionCountByMonth(habitId) }

                singleStats.value = mapHabitSingleStats(
                    completionRate.await(),
                    actionCountByWeekEntity.await(),
                    LocalDate.now(),
                    Locale.getDefault()
                )
                actionCountByWeek.value = mapActionCountByWeek(actionCountByWeekEntity.await())
                actionCountByMonth.value = mapActionCountByMonth(actionCountByMonthEntity.await())
            } catch (e: Throwable) {
                // Fail silently
                Timber.e(e)
            }
        }
    }

    fun toggleActionFromDetail(habitId: Int, action: Action, date: LocalDate) {
        viewModelScope.launch {
            toggleAction(habitId, action, date)
            fetchHabitDetails(habitId)
            fetchHabitStats(habitId)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            dao.updateHabit(habit.toEntity())
            fetchHabitDetails(habit.id)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            dao.deleteHabit(habit.toEntity())
            backNavigationEvent.call()
        }
    }

    private suspend fun toggleAction(
        habitId: Int,
        updatedAction: Action,
        date: LocalDate,
    ) {
        if (updatedAction.toggled) {
            val newAction = com.ofalvai.habittracker.persistence.entity.Action(
                habit_id = habitId,
                timestamp = LocalDateTime.of(date, LocalTime.now())
                    .toInstant(OffsetDateTime.now().offset)
            )
            dao.insertAction(newAction)
        } else {
            dao.deleteAction(updatedAction.id)
        }
    }

}