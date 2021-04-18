package com.ofalvai.habittracker.ui.habitdetail

import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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

    val view = remember {
        CalendarView(context).apply {
            orientation = LinearLayout.HORIZONTAL
            scrollMode = ScrollMode.PAGED
            dayViewResource = R.layout.item_calendar_day
        }
    }

    AndroidView({ view }) { calendarView ->
        calendarView.dayBinder = HabitDayBinder(habitColor, actions, onDayToggle)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(yearMonth, yearMonth, firstDayOfWeek)
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
            onDayToggle(day.date, action.copy(toggled = !action.toggled))
        }
    }

    fun bind(day: CalendarDay, habitColor: Color, action: Action) {
        this.day = day
        this.action = action

        if (day.owner == DayOwner.THIS_MONTH) {
            textView.text = day.date.dayOfMonth.toString()

            if (action.toggled) {
                textView.setBackgroundColor(habitColor.toColorInt())
            } else {
                textView.setBackgroundColor(backgroundColor)
            }

            if (day.date == LocalDate.now()) {
                textView.typeface = Typeface.DEFAULT_BOLD
            } else {
                textView.typeface = Typeface.DEFAULT
            }
        } else {
            textView.visibility = View.INVISIBLE
        }
    }
}

private class HabitDayBinder(
    private val habitColor: Color,
    private val actions: List<Action>,
    private val onDayToggle: (LocalDate, Action) -> Unit
) : DayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view, onDayToggle)

    override fun bind(container: DayViewContainer, day: CalendarDay) {
        val actionOnDay = actions.find {
            val dateOfAction = LocalDateTime
                .ofInstant(it.timestamp, ZoneId.systemDefault())
                .toLocalDate()
            dateOfAction == day.date
        } ?: Action(0, false, null)
        container.bind(day, habitColor, actionOnDay)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun Color.toColorInt(): Int {
    // This isn't 100% correct, but works with SRGB color space
    return (value shr 32).toInt()
}
