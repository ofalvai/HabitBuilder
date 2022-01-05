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

package com.ofalvai.habittracker.ui.insights.component

import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.common.CalendarDayLegend
import com.ofalvai.habittracker.ui.common.CalendarPager
import com.ofalvai.habittracker.ui.common.ErrorView
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.insights.InsightsViewModel
import com.ofalvai.habittracker.ui.model.HeatmapMonth
import com.ofalvai.habittracker.ui.theme.AppIcons
import com.ofalvai.habittracker.ui.theme.gray2
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun Heatmap(viewModel: InsightsViewModel) {

    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    val heatmapState by viewModel.heatmapState.collectAsState()

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
        Column {
            CalendarPager(
                yearMonth = yearMonth,
                onPreviousClick = onPreviousMonth,
                onNextClick = onNextMonth
            )

            CalendarDayLegend()

            when (heatmapState) {
                is Result.Success -> {
                    val heatmapData = (heatmapState as Result.Success).value // no smart cast :'(
                    val enoughData = hasEnoughData(heatmapData)

                    if (!enoughData) {
                        EmptyView()
                    }
                    HeatmapCalendar(yearMonth, heatmapData)

                    if (enoughData) {
                        HeatmapLegend(
                            heatmapData,
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        )
                    }
                }
                Result.Loading -> {
                    // Avoid emitting another HeatmapCalendar() here because it an expensive
                    // composable (wraps an AndroidView which contains a RecyclerView)
                    // Yes, the layout will jump when transitioning from Loading -> Success,
                    // but it's still better than janky animations
                }
                is Result.Failure -> {
                    ErrorView(label = stringResource(R.string.insights_heatmap_error))
                }
            }
        }
    }
}

@Composable
private fun HeatmapCalendar(
    yearMonth: YearMonth,
    heatmapData: HeatmapMonth
) {
    var showPopup by remember { mutableStateOf(false) }
    var popupActionCount by remember { mutableStateOf(0) }
    val onDayClick: (HeatmapMonth.BucketInfo) -> Unit = {
        showPopup = true
        popupActionCount = it.value
    }

    if (showPopup) {
        DayPopup(popupActionCount, onDismiss = { showPopup = false })
    }

    val context = LocalContext.current
    val primaryColor = MaterialTheme.colors.primary
    val view = remember {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        CalendarView(context).apply {
            orientation = LinearLayout.HORIZONTAL
            scrollMode = ScrollMode.PAGED
            dayViewResource = R.layout.item_calendar_day_heatmap
            dayBinder = HeatmapDayBinder(heatmapData, onDayClick, primaryColor)
            setup(startMonth = yearMonth, endMonth = yearMonth, firstDayOfWeek)
        }
    }

    AndroidView({ view }) { calendarView ->
        val binder = calendarView.dayBinder as HeatmapDayBinder
        // This recomposition happens quite often, but we should only reload when relevant data changes
        if (heatmapData != binder.heatmapData) {
            binder.heatmapData = heatmapData
            calendarView.updateMonthRange(startMonth = yearMonth, endMonth = yearMonth)
            calendarView.notifyMonthChanged(yearMonth)
        }
    }
}

@Composable
private fun DayPopup(
    actionCount: Int,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
    ) {
        val shape = RoundedCornerShape(percent = 50)
        Box(
            Modifier
                .shadow(4.dp, shape)
                .clip(shape)
                .background(MaterialTheme.colors.background)
                .padding(8.dp)
        ) {
            Text(
                text = LocalContext.current.resources.getQuantityString(
                    R.plurals.insights_heatmap_popup_action_count,
                    actionCount,
                    actionCount
                ),
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
private fun HeatmapLegend(
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
            Modifier.border(1.dp, MaterialTheme.colors.gray2).alignByBaseline()
        ) {
            heatmapData.bucketMaxValues.forEach {
                val bucketIndex = it.first
                val maxValue = it.second
                val backgroundColor = MaterialTheme.colors.primary.adjustToBucketIndex(
                    bucketIndex, heatmapData.bucketCount
                )
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

@Composable
private fun EmptyView() {
    Text(
        text = stringResource(R.string.insights_heatmap_empty_label),
        style = MaterialTheme.typography.caption.copy(fontStyle = FontStyle.Italic),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

private fun hasEnoughData(heatmapData: HeatmapMonth): Boolean {
    return heatmapData.totalHabitCount > 0
}

private class HeatmapDayBinder(
    var heatmapData: HeatmapMonth,
    private val onDayClick: (HeatmapMonth.BucketInfo) -> Unit,
    private val primaryColor: Color
) : DayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view, onDayClick)

    override fun bind(container: DayViewContainer, day: CalendarDay) {
        val dayData = heatmapData.dayMap[day.date] ?: HeatmapMonth.BucketInfo(0, 0)
        container.bind(day, dayData, heatmapData.bucketCount, primaryColor)
    }
}

private class DayViewContainer(
    view: View,
    private val onDayClick: (HeatmapMonth.BucketInfo) -> Unit
) : ViewContainer(view) {

    val textView = view.findViewById<TextView>(R.id.calendarDayText)!!

    lateinit var day: CalendarDay
    lateinit var bucketInfo: HeatmapMonth.BucketInfo

    init {
        textView.setOnClickListener {
            onDayClick(bucketInfo)
        }
    }

    fun bind(
        day: CalendarDay,
        bucketInfo: HeatmapMonth.BucketInfo,
        bucketCount: Int,
        primaryColor: Color
    ) {
        this.day = day
        this.bucketInfo = bucketInfo

        val today = LocalDate.now()
        val color = primaryColor.adjustToBucketIndex(bucketInfo.bucketIndex, bucketCount)
        textView.setBackgroundColor(color.toColorInt())

        textView.visibility = if (day.owner == DayOwner.THIS_MONTH) {
            View.VISIBLE
        } else {
            View.INVISIBLE // View.GONE would mess up the grid-like layout
        }

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

@ColorInt
private fun Color.adjustToBucketIndex(index: Int, bucketCount: Int): Color {
    if (index > 0 && index >= bucketCount) {
        throw IllegalArgumentException("Bucket index ($index) outside of bucket range (count=$bucketCount)")
    }

    return if (bucketCount == 0) {
        return Color.Transparent
    } else {
        this.copy(alpha = index / bucketCount.toFloat())
    }
}