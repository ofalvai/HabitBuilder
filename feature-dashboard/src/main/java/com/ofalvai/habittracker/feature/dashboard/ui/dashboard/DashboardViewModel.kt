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

package com.ofalvai.habittracker.feature.dashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.core.common.AppPreferences
import com.ofalvai.habittracker.core.common.OnboardingManager
import com.ofalvai.habittracker.core.common.OnboardingState
import com.ofalvai.habittracker.core.common.Telemetry
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.HabitId
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.feature.dashboard.mapper.mapHabitEntityToModel
import com.ofalvai.habittracker.feature.dashboard.repo.ActionRepository
import com.ofalvai.habittracker.feature.dashboard.ui.model.DashboardConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import com.ofalvai.habittracker.core.database.entity.HabitWithActions as HabitWithActionsEntity

enum class DashboardEvent {
    ToggleActionError,
    MoveHabitError,
    ActionPerformed
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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dao: HabitDao,
    private val actionRepository: ActionRepository,
    private val appPreferences: AppPreferences,
    private val telemetry: Telemetry,
    private val onboardingManager: OnboardingManager
) : ViewModel() {

    val habitsWithActions: Flow<Result<ImmutableList<HabitWithActions>>> = dao
        .getActiveHabitsWithActions()
        .distinctUntilChanged()
        .map<List<HabitWithActionsEntity>, Result<ImmutableList<HabitWithActions>>> {
            Result.Success(mapHabitEntityToModel(it))
        }
        .catch {
            telemetry.logNonFatal(it)
            emit(Result.Failure(it))
        }

    var dashboardConfig
        get() = parseDashboardConfig(appPreferences.dashboardConfig)
        set(value) {
            appPreferences.dashboardConfig = value.toString()
        }

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

    fun toggleAction(habitId: Int, action: Action, daysInPast: Int) {
        viewModelScope.launch {
            try {
                if (action.toggled) {
                    eventChannel.send(DashboardEvent.ActionPerformed)
                }

                val date = LocalDate.now().minusDays(daysInPast.toLong())
                actionRepository.toggleAction(habitId, action, date)
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

    private fun parseDashboardConfig(stringValue: String?): DashboardConfig {
        if (stringValue == null) {
            return DashboardConfig.FiveDay
        }
        return DashboardConfig.valueOf(stringValue)
    }
}