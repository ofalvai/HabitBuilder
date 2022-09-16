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

package com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitId
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.ItemMoveEvent
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.DayLegend
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view.ReorderableHabitList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import java.time.LocalDate

@Composable
fun CompactHabitList(
    habits: ImmutableList<HabitWithActions>,
    onActionToggle: (Action, Habit, Int) -> Unit,
    onHabitClick: (HabitId) -> Unit,
    onAddHabitClick: () -> Unit,
    onMove: (ItemMoveEvent) -> Unit,
) {
    Column {
        DayLegend(
            modifier = Modifier.fillMaxWidth(),
            mostRecentDay = LocalDate.now(),
            pastDayCount = Constants.DAY_COUNT - 1
        )

        ReorderableHabitList(
            habits, Arrangement.Top, onMove, onAddHabitClick
        ) { item, reorderState ->
            HabitItem(
                habit = item.habit,
                actions = item.actions.takeLast(Constants.DAY_COUNT).toImmutableList(),
                onActionToggle = onActionToggle,
                onDetailClick = onHabitClick,
                // Null and 0 drag offset is intentionally treated as the same because dragging
                // is using the same gesture detection as the long-press Action toggle modifier
                dragOffset = reorderState.offsetByKey(item.habit.id)?.y?.toFloat() ?: 0f,
                modifier = Modifier.detectReorderAfterLongPress(reorderState)
            )
        }
    }
}