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

package com.ofalvai.habittracker.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.mapHabitEntityToArchivedModel
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.persistence.entity.HabitById
import com.ofalvai.habittracker.telemetry.Telemetry
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.model.ArchivedHabit
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

enum class ArchiveEvent {
    UnarchiveError,
    DeleteError
}

class ArchiveViewModel(
    private val dao: HabitDao,
    private val telemetry: Telemetry
): ViewModel() {

    private val eventChannel = Channel<ArchiveEvent>(Channel.BUFFERED)
    val archiveEvent = eventChannel.receiveAsFlow()

    val archivedHabitList: Flow<Result<List<ArchivedHabit>>> = dao
        .getArchivedHabitsWithActions()
        .map<List<HabitWithActionsEntity>, Result<List<ArchivedHabit>>> {
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
}