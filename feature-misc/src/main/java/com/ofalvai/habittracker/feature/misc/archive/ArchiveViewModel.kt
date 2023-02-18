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

package com.ofalvai.habittracker.feature.misc.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.core.common.Telemetry
import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.database.entity.HabitById
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.feature.misc.archive.model.ArchivedHabit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ofalvai.habittracker.core.database.entity.HabitWithActions as HabitWithActionsEntity

enum class ArchiveEvent {
    UnarchiveError,
    DeleteError
}

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val dao: HabitDao,
    private val telemetry: Telemetry
): ViewModel() {

    private val eventChannel = Channel<ArchiveEvent>(Channel.BUFFERED)
    val archiveEvent = eventChannel.receiveAsFlow()

    val archivedHabitList: Flow<Result<ImmutableList<ArchivedHabit>>> = dao
        .getArchivedHabitsWithActions()
        .map<List<HabitWithActionsEntity>, Result<ImmutableList<ArchivedHabit>>> {
            Result.Success(mapHabitEntityToArchivedModel(it))
        }
        .catch {
            telemetry.logNonFatal(it)
            emit(Result.Failure(it))
        }

    fun unarchiveHabit(habit: ArchivedHabit) {
        viewModelScope.launch {
            try {
                dao.unarchiveHabit(habit.id)
            } catch (e: Throwable) {
                telemetry.logNonFatal(e)
                eventChannel.send(ArchiveEvent.UnarchiveError)
            }
        }
    }

    fun deleteHabit(habit: ArchivedHabit) {
        viewModelScope.launch {
            try {
                dao.deleteHabit(HabitById(habit.id))
            } catch (e: Throwable) {
                telemetry.logNonFatal(e)
                eventChannel.send(ArchiveEvent.DeleteError)
            }
        }
    }

    private fun mapHabitEntityToArchivedModel(habitsWithActions: List<HabitWithActionsEntity>): ImmutableList<ArchivedHabit> {
        return habitsWithActions.map {
            ArchivedHabit(
                id = it.habit.id,
                name = it.habit.name,
                totalActionCount = it.actions.size,
                lastAction = it.actions.lastOrNull()?.timestamp
            )
        }.toImmutableList()
    }
}