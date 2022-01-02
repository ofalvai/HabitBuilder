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

package com.ofalvai.habittracker.ui.habitdetail

import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.model.Action
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun HabitCalendar(
    yearMonth: YearMonth,
    habitColor: Color,
    actions: List<Action>,
    onDayToggle: (LocalDate, Action) -> Unit
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colors.onSurface
    val textColorActive = MaterialTheme.colors.surface

    val view = remember {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        CalendarView(context).apply {
            orientation = LinearLayout.HORIZONTAL
            scrollMode = ScrollMode.PAGED
            dayViewResource = R.layout.item_calendar_day
            dayBinder = HabitDayBinder(habitColor, textColor, textColorActive, onDayToggle)
            itemAnimator = DefaultItemAnimator().apply {
                // Avoid flashes on recomposition
                supportsChangeAnimations = false
            }
            setup(startMonth = yearMonth, endMonth = yearMonth, firstDayOfWeek)
        }
    }

    AndroidView({ view }) { calendarView ->
        (calendarView.dayBinder as HabitDayBinder).also {
            it.habitColor = habitColor
            it.currentMonthActions = actions
        }

        calendarView.updateMonthRange(startMonth = yearMonth, endMonth = yearMonth)
        calendarView.notifyCalendarChanged()
    }
}

private class HabitDayBinder(
    var habitColor: Color,
    private val textColor: Color,
    private val textColorActive: Color,
    private val onDayToggle: (LocalDate, Action) -> Unit
) : DayBinder<DayViewContainer> {

    var currentMonthActions: List<Action> = emptyList()

    override fun create(view: View) = DayViewContainer(view, onDayToggle)

    override fun bind(container: DayViewContainer, day: CalendarDay) {
        val actionOnDay = currentMonthActions.find {
            val dateOfAction = LocalDateTime
                .ofInstant(it.timestamp, ZoneId.systemDefault())
                .toLocalDate()
            dateOfAction == day.date
        } ?: Action(0, false, null)
        container.bind(day, habitColor, actionOnDay, textColor, textColorActive)
    }
}

private class DayViewContainer(
    view: View,
    private val onDayToggle: (LocalDate, Action) -> Unit
) : ViewContainer(view) {

    val textView = view.findViewById<TextView>(R.id.calendarDayText)!!
    val backgroundColor = ContextCompat.getColor(view.context, R.color.calendarCellBackground)

    lateinit var day: CalendarDay
    lateinit var action: Action

    init {
        textView.setOnClickListener {
            if (!day.date.isAfter(LocalDate.now())) {
                onDayToggle(day.date, action.copy(toggled = !action.toggled))
            }
        }
    }

    fun bind(
        day: CalendarDay,
        habitColor: Color,
        action: Action,
        textColor: Color,
        textColorActive: Color
    ) {
        this.day = day
        this.action = action

        val today = LocalDate.now()

        textView.visibility = if (day.owner == DayOwner.THIS_MONTH) {
            View.VISIBLE
        } else {
            View.INVISIBLE // View.GONE would mess up the grid-like layout
        }

        textView.setBackgroundColor(
            if (action.toggled) habitColor.toColorInt() else backgroundColor
        )
        textView.setTextColor(
            (if (action.toggled) textColorActive else textColor).toColorInt()
        )

        textView.alpha = if (day.date.isAfter(today)) 0.5f else 1f

        textView.typeface = if (day.date == today) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

        textView.paintFlags = if (day.date == today) {
            textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        } else {
            textView.paintFlags
        }
        textView.text = if (day.owner == DayOwner.THIS_MONTH) {
            day.date.dayOfMonth.toString()
        } else {
            null
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun Color.toColorInt(): Int {
    // This isn't 100% correct, but works with SRGB color space
    return (value shr 32).toInt()
}
