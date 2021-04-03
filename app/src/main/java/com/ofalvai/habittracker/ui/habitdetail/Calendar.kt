package com.ofalvai.habittracker.ui.habitdetail

import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import java.time.*
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
            Icon(
                Icons.Rounded.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.calendar_previous_month)
            )
        }

        Text(text = label)

        IconButton(onClick = onNextClick) {
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(R.string.calendar_next_month)
            )
        }
    }
}

@Composable
fun CalendarDayLegend(weekFields: WeekFields) {
    // TODO: use a Grid-like layout for perfect alignment
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (0..6).map {
            val day = weekFields.firstDayOfWeek.plus(it.toLong())
            val label = day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            Text(
                text = label,
                style = MaterialTheme.typography.caption
            )
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

@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewCalendarDayLegend() {
    HabitTrackerTheme {
        CalendarDayLegend(WeekFields.ISO)
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
                textView.setBackgroundColor(habitColor.toColorInt())
            } else {
                textView.setBackgroundColor(Color.Black.copy(alpha = 0.05f).toColorInt())
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
