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
import com.ofalvai.habittracker.mapper.mapHabitEntityToModel
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.telemetry.Telemetry
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

class ArchiveViewModel(
    private val dao: HabitDao,
    private val telemetry: Telemetry
): ViewModel() {

    val archivedHabitList: Flow<Result<List<HabitWithActions>>> = dao
        .getArchivedHabitsWithActions()
        .map<List<HabitWithActionsEntity>, Result<List<HabitWithActions>>> {
            Result.Success(mapHabitEntityToModel(it))
        }
        .catch {
            telemetry.logNonFatal(it)
            emit(Result.Failure(it))
        }

    fun unarchiveHabit(habit: Habit) {

    }

}