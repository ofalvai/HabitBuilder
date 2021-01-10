package com.ofalvai.habittracker.ui.habitdetail

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.ofalvai.habittracker.R
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun HabitCalendar(
    habitColor: Color
) {
    val context = AmbientContext.current

    val view = remember {
        CalendarView(context).apply {
            orientation = LinearLayout.HORIZONTAL
            scrollMode = ScrollMode.PAGED
            dayViewResource = R.layout.item_calendar_day

            val currentMonth = YearMonth.now()
            // TODO: based on action entries, plus a few empty months (to allow past edits)
            val firstMonth = currentMonth.minusMonths(12)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            setup(firstMonth, currentMonth, firstDayOfWeek)
            scrollToMonth(currentMonth)
        }
    }

    AndroidView({ view }) { calendarView ->
        calendarView.dayBinder = HabitDayBinder(habitColor)
    }
}

private class DayViewContainer(view: View) : ViewContainer(view) {

    val textView = view.findViewById<TextView>(R.id.calendarDayText)!!

    init {
        textView.setOnClickListener {
            // TODO
        }
    }

    fun bind(day: CalendarDay, habitColor: Color) {
        if (day.owner == DayOwner.THIS_MONTH) {
            textView.text = day.date.dayOfMonth.toString()
            textView.background = backgroundFrom(habitColor)
        } else {
            textView.visibility = View.INVISIBLE
        }
    }

    private fun backgroundFrom(habitColor: Color): Drawable {
        val background =
            ContextCompat.getDrawable(view.context, R.drawable.bg_calendar_item_active)!!
        background.setTint(habitColor.toColorInt())
        return background
    }
}

private class HabitDayBinder(
    private val habitColor: Color
) : DayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)

    override fun bind(container: DayViewContainer, day: CalendarDay) {
        container.bind(day, habitColor)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun Color.toColorInt(): Int {
    // This isn't 100% correct, but works with SRGB color space
    return (value shr 32).toInt()
}
