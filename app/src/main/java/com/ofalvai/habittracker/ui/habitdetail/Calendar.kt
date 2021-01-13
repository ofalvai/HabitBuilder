package com.ofalvai.habittracker.ui.habitdetail

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.HabitTrackerTheme
import com.ofalvai.habittracker.ui.model.Action
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun HabitCalendar(
    yearMonth: YearMonth,
    habitColor: Color,
    actions: List<Action>,
    onDayToggle: (LocalDate, Action) -> Unit
) {
    val context = AmbientContext.current

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

@Composable
fun CalendarPager(
    yearMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val year = yearMonth.year
    val label = if (year == Year.now().value) month else "$month $year"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Filled.KeyboardArrowLeft)
        }

        Text(text = label)

        IconButton(onClick = onNextClick) {
            Icon(Icons.Filled.KeyboardArrowRight)
        }
    }
}

@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewCalendarPager() {
    HabitTrackerTheme {
        CalendarPager(
            yearMonth = YearMonth.now(),
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}

private class DayViewContainer(
    view: View,
    private val onDayToggle: (LocalDate, Action) -> Unit
) : ViewContainer(view) {

    val textView = view.findViewById<TextView>(R.id.calendarDayText)!!

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
                textView.background = backgroundFrom(habitColor)
            } else {
                textView.background = null
            }
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
