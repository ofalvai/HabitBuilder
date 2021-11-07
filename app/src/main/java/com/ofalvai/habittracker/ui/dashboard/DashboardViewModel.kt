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
import com.ofalvai.habittracker.mapper.mapHabitEntityToModel
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.telemetry.Telemetry
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.HabitId
import com.ofalvai.habittracker.ui.model.HabitWithActions
import com.ofalvai.habittracker.ui.model.OnboardingState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

enum class DashboardEvent {
    ToggleActionError,
    MoveHabitError
}

/**
 * Information about moving up/down an item in the habit list.
 * When a single item move has a distance of more than 1, the view should post one event for each
 * step.
 */
data class ItemMoveEvent(
    val firstHabitId: HabitId,
    val secondHabitId: HabitId
)

class DashboardViewModel(
    private val dao: HabitDao,
    appPreferences: AppPreferences,
    private val telemetry: Telemetry,
    private val onboardingManager: OnboardingManager
) : ViewModel() {

    val habitsWithActions: Flow<Result<List<HabitWithActions>>> = dao
        .getHabitsWithActions()
        .distinctUntilChanged()
        .map<List<HabitWithActionsEntity>, Result<List<HabitWithActions>>> {
            Result.Success(mapHabitEntityToModel(it))
        }
        .catch {
            telemetry.logNonFatal(it)
            emit(Result.Failure(it))
        }

    var dashboardConfig by appPreferences::dashboardConfig

    private val eventChannel = Channel<DashboardEvent>(Channel.BUFFERED)
    val dashboardEvent = eventChannel.receiveAsFlow()

    val onboardingState: StateFlow<OnboardingState?> = onboardingManager.state

    /**
     * Queue of item move events that should be persisted to the DB. The view posts events into
     * the channel and the ViewModel consumes each event to maintain consistency.
     */
    private val itemMoveChannel = Channel<ItemMoveEvent>(Channel.UNLIMITED)

    init {
        consumeReorderEvents()
    }

    fun toggleActionFromDashboard(habitId: Int, action: Action, date: LocalDate) {
        viewModelScope.launch {
            try {
                toggleAction(habitId, action, date)
                onboardingManager.firstActionCompleted()
            } catch (e: Throwable) {
                telemetry.logNonFatal(e)
                eventChannel.send(DashboardEvent.ToggleActionError)
            }
        }
    }

    fun persistItemMove(event: ItemMoveEvent) {
        viewModelScope.launch {
            itemMoveChannel.send(event)
        }
    }

    private fun consumeReorderEvents() {
        viewModelScope.launch {
            itemMoveChannel.consumeEach {
                try {
                    val habits = dao.getHabitPair(it.firstHabitId, it.secondHabitId)
                    val firstHabitOrder = habits.first { h -> h.id == it.firstHabitId }.order
                    val secondHabitOrder = habits.first { h -> h.id == it.secondHabitId }.order
                    dao.updateHabitOrders(
                        id1 = it.firstHabitId,
                        order1 = secondHabitOrder,
                        id2 = it.secondHabitId,
                        order2 = firstHabitOrder
                    )
                } catch (e: Throwable) {
                    telemetry.logNonFatal(e)
                    eventChannel.send(DashboardEvent.MoveHabitError)
                }
            }
        }
    }

    // TODO: duplicated across Dashboard + HabitDetails
    private suspend fun toggleAction(
        habitId: Int,
        updatedAction: Action,
        date: LocalDate,
    ) {
        if (updatedAction.toggled) {
            val newAction = ActionEntity(
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