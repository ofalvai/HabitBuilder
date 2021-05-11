package com.ofalvai.habittracker.ui.insights.component

import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.ofalvai.habittracker.ui.theme.AppIcons
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

private val initialState = HeatmapMonth(YearMonth.now(), emptyMap(), 0, 0, emptyList())

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

    InsightCard(
        iconPainter = AppIcons.Heatmap,
        title = stringResource(R.string.insights_heatmap_title),
        description = stringResource(R.string.insights_heatmap_description),
    ) {
        // TODO: too many recompositions
        Column {
            CalendarPager(
                yearMonth = yearMonth,
                onPreviousClick = onPreviousMonth,
                onNextClick = onNextMonth
            )

            CalendarDayLegend(weekFields = WeekFields.of(Locale.getDefault()))

            HeatmapCalendar(yearMonth, heatmapData)

            HeatmapLegend(heatmapData, modifier = Modifier.align(Alignment.End).padding(top = 8.dp))
        }
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

@Composable
fun HeatmapLegend(
    heatmapData: HeatmapMonth,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Text(
            text = stringResource(R.string.insights_heatmap_legend_label),
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(end = 8.dp).alignByBaseline()
        )

        Row(
            Modifier.border(1.dp, MaterialTheme.colors.onSurface).alignByBaseline()
        ) {
            heatmapData.bucketMaxValues.forEach {
                val bucketIndex = it.first
                val maxValue = it.second
                val backgroundColor = bucketIndexToColor(bucketIndex, heatmapData.bucketCount)
                Box(
                    Modifier.background(backgroundColor).size(24.dp)
                ) {
                    Text(
                        text = maxValue.toString(),
                        color = contentColorFor(backgroundColor),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxSize().padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

private class DayViewContainer(
    view: View,
) : ViewContainer(view) {

    val textView = view.findViewById<TextView>(R.id.calendarDayText)!!

    lateinit var day: CalendarDay

    fun bind(day: CalendarDay, bucketInfo: HeatmapMonth.BucketInfo, bucketCount: Int) {
        this.day = day

        val color = bucketIndexToColor(bucketInfo.bucketIndex, bucketCount)
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
        container.bind(day, dayData, heatmapData.bucketCount)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun Color.toColorInt(): Int {
    // This isn't 100% correct, but works with SRGB color space
    return (value shr 32).toInt()
}

private fun bucketIndexToColor(index: Int, bucketCount: Int): Color {
    if (index > 0 && index >= bucketCount) {
        throw IllegalArgumentException("Bucket index ($index) outside of bucket range (count=$bucketCount)")
    }

    return if (bucketCount == 0) {
        return Color.Transparent
    } else {
        Color.Gray.copy(
            alpha = index / bucketCount.toFloat()
        )
    }
}