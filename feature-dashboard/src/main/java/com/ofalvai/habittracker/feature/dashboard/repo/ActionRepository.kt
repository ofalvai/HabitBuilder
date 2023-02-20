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

package com.ofalvai.habittracker.feature.dashboard.repo

import com.ofalvai.habittracker.core.database.HabitDao
import com.ofalvai.habittracker.core.model.Action
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton
import com.ofalvai.habittracker.core.database.entity.Action as ActionEntity

@Singleton
class ActionRepository @Inject constructor(
    private val dao: HabitDao,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun toggleAction(
        habitId: Int,
        updatedAction: Action,
        date: LocalDate,
    ) {
        withContext(dispatcher) {
            if (updatedAction.toggled) {
                val newAction = ActionEntity(
                    habit_id = habitId,
                    timestamp = LocalDateTime.of(date, LocalTime.now())
                        .toInstant(OffsetDateTime.now().offset)
                )
                dao.insertActions(listOf(newAction))
            } else {
                dao.deleteAction(updatedAction.id)
            }
        }
    }
}