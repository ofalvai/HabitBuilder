package com.ofalvai.habittracker.ui.insights.component

import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.common.CalendarDayLegend
import com.ofalvai.habittracker.ui.common.CalendarPager
import com.ofalvai.habittracker.ui.insights.InsightsViewModel
import com.ofalvai.habittracker.ui.model.HeatmapMonth
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

private val initialState = HeatmapMonth(YearMonth.now(), emptyMap(), 0)

@Composable
fun Heatmap(viewModel: InsightsViewModel) {

    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    val heatmapData by viewModel.heatmapData.observeAsState(initialState)

    val onPreviousMonth = {
        yearMonth = yearMonth.minusMonths(1)
        viewModel.fetchHeatmap(yearMonth)
    }
    val onNextMonth = {
        yearMonth = yearMonth.plusMonths(1)
        viewModel.fetchHeatmap(yearMonth)
    }

    // TODO: too many recompositions
    Column(Modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
        CalendarPager(
            yearMonth = yearMonth,
            onPreviousClick = onPreviousMonth,
            onNextClick = onNextMonth
        )

        CalendarDayLegend(weekFields = WeekFields.of(Locale.getDefault()))

        HeatmapCalendar(yearMonth, heatmapData)
    }
}

@Composable
fun HeatmapCalendar(
    yearMonth: YearMonth,
    heatmapData: HeatmapMonth
) {
    val context = LocalContext.current

    val view = remember {
        CalendarView(context).apply {
            orientation = LinearLayout.HORIZONTAL
            scrollMode = ScrollMode.PAGED
            dayViewResource = R.layout.item_calendar_day_heatmap
        }
    }

    AndroidView({ view }) { calendarView ->
        calendarView.dayBinder = HabitDayBinder(heatmapData)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(yearMonth, yearMonth, firstDayOfWeek)
    }
}

private class DayViewContainer(
    view: View,
) : ViewContainer(view) {

    val textView = view.findViewById<TextView>(R.id.calendarDayText)!!

    lateinit var day: CalendarDay

    init {

    }

    fun bind(day: CalendarDay, bucketInfo: HeatmapMonth.BucketInfo) {
        this.day = day

        val color = Color.Gray.copy(alpha = bucketInfo.bucketIndex / 5f)
        textView.setBackgroundColor(color.toColorInt())

        if (day.owner == DayOwner.THIS_MONTH) {
            textView.text = day.date.dayOfMonth.toString()

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
    private val heatmapData: HeatmapMonth
) : DayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)

    override fun bind(container: DayViewContainer, day: CalendarDay) {
        val dayData = heatmapData.dayMap[day.date] ?: HeatmapMonth.BucketInfo(0, 0)
        container.bind(day, dayData)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun Color.toColorInt(): Int {
    // This isn't 100% correct, but works with SRGB color space
    return (value shr 32).toInt()
}