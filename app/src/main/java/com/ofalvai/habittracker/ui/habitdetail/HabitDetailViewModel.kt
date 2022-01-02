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

package com.ofalvai.habittracker.ui.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.*
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.telemetry.Telemetry
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.dashboard.OnboardingManager
import com.ofalvai.habittracker.ui.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity

private val initialSingleStats = SingleStats(null, 0, 0, 0f)
private val initialChartData = ActionCountChart(emptyList(), ActionCountChart.Type.Weekly)

enum class HabitDetailEvent {
    BackNavigation
}

class HabitDetailViewModel(
    private val dao: HabitDao,
    private val telemetry: Telemetry,
    onboardingManager: OnboardingManager
) : ViewModel() {

    val habitWithActions = MutableStateFlow<Result<HabitWithActions>>(Result.Loading)
    val singleStats = MutableStateFlow(initialSingleStats)
    val chartData = MutableStateFlow<Result<ActionCountChart>>(Result.Success(initialChartData))

    private val eventChannel = Channel<HabitDetailEvent>(Channel.BUFFERED)
    val habitDetailEvent = eventChannel.receiveAsFlow()

    private val actionCountByWeek = MutableStateFlow<List<ActionCountByWeek>>(emptyList())
    private val actionCountByMonth = MutableStateFlow<List<ActionCountByMonth>>(emptyList())

    // Store loaded habit's order to convert the model back to entity later
    private var habitOrder: Int? = null

    init {
        onboardingManager.habitDetailsOpened()
    }

    fun fetchHabitDetails(habitId: Int): Job {
        return viewModelScope.launch {
            try {
                val habit = dao.getHabitWithActions(habitId).let {
                    habitOrder = it.habit.order

                    // TODO: unify this with the regular mapping (where empty day action are filled)
                    HabitWithActions(
                        Habit(it.habit.id, it.habit.name, it.habit.color.toUIColor(), it.habit.notes),
                        it.actions.map { action ->
                            Action(action.id, toggled = true, timestamp = action.timestamp)
                        },
                        it.actions.size,
                        actionsToHistory(it.actions)
                    )
                }
                habitWithActions.value = Result.Success(habit)
            } catch (e: Throwable) {
                telemetry.logNonFatal(e)
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
                if (chartData.value is Result.Success) {
                    switchChartType((chartData.value as Result.Success<ActionCountChart>).value.type)
                }
            } catch (e: Throwable) {
                // Fail silently
                telemetry.logNonFatal(e)
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
            dao.updateHabit(habit.toEntity(order = habitOrder ?: 0, archived = false))
            fetchHabitDetails(habit.id)
        }
    }

    fun archiveHabit(habit: Habit) {
        viewModelScope.launch {
            dao.updateHabit(habit.toEntity(order = habitOrder ?: 0, archived = true))
            eventChannel.send(HabitDetailEvent.BackNavigation)
        }
    }

    fun switchChartType(newType: ActionCountChart.Type) {
        try {
            val items = when (newType) {
                ActionCountChart.Type.Weekly -> mapActionCountByWeekListToItemList(
                    actionCountByWeek.value,
                    LocalDate.now(),
                    Locale.getDefault()
                )
                ActionCountChart.Type.Monthly -> mapActionCountByMonthListToItemList(
                    actionCountByMonth.value,
                    LocalDate.now()
                )
            }
            chartData.value = Result.Success(ActionCountChart(items, newType))
        } catch (e: Throwable) {
            chartData.value = Result.Failure(e)
            telemetry.logNonFatal(e)
        }
    }

    private suspend fun toggleAction(
        habitId: Int,
        updatedAction: Action,
        date: LocalDate,
    ) {
        if (updatedAction.toggled) {
            val newAction = ActionEntity(
                habit_id = habitId,
                timestamp = date.atStartOfDay()
                    .toInstant(OffsetDateTime.now().offset)
            )
            dao.insertAction(newAction)
        } else {
            dao.deleteAction(updatedAction.id)
        }
    }

}