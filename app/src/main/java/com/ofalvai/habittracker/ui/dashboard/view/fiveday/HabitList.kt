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

import android.os.Vibrator
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.ofalvai.habittracker.ui.dashboard.ItemMoveEvent
import com.ofalvai.habittracker.ui.dashboard.view.CreateHabitButton
import com.ofalvai.habittracker.ui.dashboard.view.DayLegend
import com.ofalvai.habittracker.ui.dashboard.view.vibrateCompat
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitId
import com.ofalvai.habittracker.ui.model.HabitWithActions
import org.burnoutcrew.reorderable.*
import java.time.LocalDate

@Composable
fun FiveDayHabitList(
    habits: List<HabitWithActions>,
    onActionToggle: (Action, Habit, LocalDate) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onAddHabitClick: () -> Unit,
    onMove: (ItemMoveEvent) -> Unit
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

        val vibrator = LocalContext.current.getSystemService<Vibrator>()!!
        // An in-memory copy of the Habit list to make drag reorder a bit smoother (not perfect).
        // We update the in-memory list on every move (of distance 1), then persist to DB in the
        // background. The cache key is the original list so that any change (eg. action completion)
        // is reflected in the in-memory copy.
        val inMemoryList = remember(habits) { habits.toMutableStateList() }
        val reorderState = rememberReorderState()
        val onItemMove: (fromPos: ItemPosition, toPos: ItemPosition) -> (Unit) = { from, to ->
            vibrator.vibrateCompat(longArrayOf(0, 50))
            inMemoryList.move(from.index, to.index)
            onMove(ItemMoveEvent(from.key as HabitId, to.key as HabitId))
        }
        val canDragOver: (index: ItemPosition) -> Boolean = {
            // Last item of the list is the fixed CreateHabitButton, it's not reorderable
            it.index < inMemoryList.size
        }

        LazyColumn(
            state = reorderState.listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp),
            modifier = Modifier.reorderable(reorderState, onItemMove, canDragOver)
        ) {
            items(inMemoryList, key = { it.habit.id }) { item ->
                HabitCard(
                    habit = item.habit,
                    actions = item.actions,
                    totalActionCount = item.totalActionCount,
                    actionHistory = item.actionHistory,
                    onActionToggle = onActionToggle,
                    onDetailClick = onHabitClick,
                    // Null and 0 drag offset is intentionally treated as the same because dragging
                    // is using the same gesture detection as the long-press Action toggle modifier
                    dragOffset = reorderState.offsetByKey(item.habit.id) ?: 0f,
                    modifier = Modifier.detectReorderAfterLongPress(reorderState)
                )
            }
            item {
                CreateHabitButton(onClick = onAddHabitClick)
            }
        }
    }
}
