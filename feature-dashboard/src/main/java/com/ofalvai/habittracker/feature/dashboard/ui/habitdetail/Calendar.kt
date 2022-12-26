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

package com.ofalvai.habittracker.feature.dashboard.ui.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.ui.component.HorizontalMonthCalendar
import com.ofalvai.habittracker.core.ui.recomposition.StableHolder
import com.ofalvai.habittracker.core.ui.semantics.habitActionSemantics
import com.ofalvai.habittracker.core.ui.theme.LocalAppColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun HabitCalendar(
    yearMonth: StableHolder<YearMonth>,
    habitColor: Color,
    actions: List<Action>,
    onDayToggle: (LocalDate, Action) -> Unit,
    onMonthSwipe: (YearMonth) -> Unit
) {
    HorizontalMonthCalendar(
        yearMonth = yearMonth,
        onMonthSwipe = onMonthSwipe
    ) {calendarDay ->
        val actionOnDay = actions.find {
            val dateOfAction = LocalDateTime
                .ofInstant(it.timestamp, ZoneId.systemDefault())
                .toLocalDate()
            dateOfAction == calendarDay.date
        } ?: Action(0, false, null)
        DayCell(calendarDay, habitColor, actionOnDay, onDayToggle)
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    habitColor: Color,
    action: Action,
    onDayClick: (LocalDate, Action) -> Unit
) {
    val today = LocalDate.now()
    val backgroundColor = if (action.toggled) {
        habitColor
    } else if (day.position == DayPosition.MonthDate) {
        LocalAppColors.current.gray1
    } else Color.Transparent

    val todayModifier = if (today == day.date) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    } else Modifier
    Box(
        modifier = Modifier
            .habitActionSemantics(action)
            .padding(4.dp)
            .then(todayModifier)
            .clip(CircleShape)
            .clickable {
                if (!day.date.isAfter(LocalDate.now())) {
                    onDayClick(day.date, action.copy(toggled = !action.toggled))
                }
            }
            .aspectRatio(1f)
            .background(backgroundColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val isFaded = !action.toggled && (day.position != DayPosition.MonthDate || day.date.isAfter(today))
        val textColor = if (action.toggled) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.alpha(if (isFaded) 0.5f else 1f),
            color = textColor
        )
    }
}
