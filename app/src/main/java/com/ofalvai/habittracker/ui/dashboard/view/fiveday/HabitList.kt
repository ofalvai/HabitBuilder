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

package com.ofalvai.habittracker.ui.dashboard.view.fiveday

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.dashboard.view.CreateHabitButton
import com.ofalvai.habittracker.ui.dashboard.view.DayLegend
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import org.burnoutcrew.reorderable.*
import timber.log.Timber
import java.time.LocalDate

@Composable
fun FiveDayHabitList(
    habits: List<HabitWithActions>,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onAddHabitClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        val width = Constants.CircleSize * 5 + Constants.CirclePadding * 8
        DayLegend(
            modifier = Modifier.wrapContentWidth(Alignment.End).width(width).padding(end = 32.dp),
            mostRecentDay = LocalDate.now(),
            pastDayCount = 4
        )

        val mutableHabitList = habits.toMutableStateList()
        val reorderState = rememberReorderState()
        val onItemMove: (fromPos: ItemPosition, toPos: ItemPosition) -> (Unit) = { from, to ->
            mutableHabitList.move(from.index, to.index)
            Timber.d("Move: ${from.index} -> ${to.index}")
        }

        LazyColumn(
            state = reorderState.listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp),
            modifier = Modifier
                .reorderable(reorderState, onItemMove)
                .detectReorderAfterLongPress(reorderState)
        ) {
            items(mutableHabitList, key = { it.habit.id }) { item ->
                HabitCard(
                    habit = item.habit,
                    actions = item.actions,
                    totalActionCount = item.totalActionCount,
                    actionHistory = item.actionHistory,
                    onActionToggle = onActionToggle,
                    onDetailClick = onHabitClick,
                    dragOffset = reorderState.offsetByKey(item.habit.id)
                )
            }
            item {
                CreateHabitButton(onClick = onAddHabitClick)
            }
        }
    }
}
