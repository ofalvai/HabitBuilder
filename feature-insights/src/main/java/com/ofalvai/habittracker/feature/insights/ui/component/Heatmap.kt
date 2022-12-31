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

package com.ofalvai.habittracker.feature.insights.ui.component

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.ui.component.CalendarDayLegend
import com.ofalvai.habittracker.core.ui.component.CalendarPager
import com.ofalvai.habittracker.core.ui.component.ErrorView
import com.ofalvai.habittracker.core.ui.component.HorizontalMonthCalendar
import com.ofalvai.habittracker.core.ui.recomposition.StableHolder
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.core.ui.theme.LocalAppColors
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.feature.insights.R
import com.ofalvai.habittracker.feature.insights.model.HeatmapMonth
import com.ofalvai.habittracker.feature.insights.ui.InsightsIcons
import com.ofalvai.habittracker.feature.insights.ui.InsightsViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun Heatmap(viewModel: InsightsViewModel) {

    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    val heatmapState by viewModel.heatmapState.collectAsState()
    val completedHabitsAtDate by viewModel.heatmapCompletedHabitsAtDate.collectAsState()

    val onMonthChange: (YearMonth) -> Unit = {
        yearMonth = it
        viewModel.fetchHeatmap(yearMonth)
    }
    val onLoadHabitsAt: (LocalDate) -> Unit = {
        viewModel.fetchCompletedHabitsAt(it)
    }

    Heatmap(StableHolder(yearMonth), heatmapState, completedHabitsAtDate, onMonthChange, onLoadHabitsAt)
}

@Composable
fun Heatmap(
    yearMonth: StableHolder<YearMonth>,
    heatmapState: Result<HeatmapMonth>,
    completedHabitsAtDate: ImmutableList<Habit>?,
    onMonthChange: (YearMonth) -> Unit,
    onLoadHabitsAt: (LocalDate) -> Unit
) {
    InsightCard(
        iconPainter = InsightsIcons.Heatmap,
        title = stringResource(R.string.insights_heatmap_title),
        description = stringResource(R.string.insights_heatmap_description),
    ) {
        Column {
            CalendarPager(
                yearMonth = yearMonth,
                onPreviousClick = { onMonthChange(yearMonth.item.minusMonths(1)) },
                onNextClick = { onMonthChange(yearMonth.item.plusMonths(1)) }
            )

            CalendarDayLegend()

            when (heatmapState) {
                is Result.Success -> {
                    val heatmapData = heatmapState.value
                    val enoughData = hasEnoughData(heatmapData)

                    if (!enoughData) {
                        EmptyView()
                    }
                    HeatmapCalendar(yearMonth, heatmapData, completedHabitsAtDate, onLoadHabitsAt, onMonthChange)

                    if (enoughData) {
                        HeatmapLegend(
                            heatmapData,
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        )
                    }
                }

                Result.Loading -> {
                    Spacer(Modifier.height(280.dp))
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
    yearMonth: StableHolder<YearMonth>,
    heatmapData: HeatmapMonth,
    completedHabitsAtDate: ImmutableList<Habit>?,
    onLoadHabitsAt: (LocalDate) -> Unit,
    onMonthSwipe: (YearMonth) -> Unit
) {
    var showPopup by remember { mutableStateOf(false) }
    val onDayClick: (LocalDate) -> Unit = {
        onLoadHabitsAt(it)
        showPopup = true
    }

    if (showPopup && completedHabitsAtDate != null) {
        DayPopup(completedHabitsAtDate, onDismiss = { showPopup = false })
    }

    HorizontalMonthCalendar(yearMonth, onMonthSwipe) {
        val dayData = heatmapData.dayMap[it.date] ?: HeatmapMonth.BucketInfo(0, 0)
        DayCell(it, dayData, heatmapData.bucketCount, onDayClick)
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    bucketInfo: HeatmapMonth.BucketInfo,
    bucketCount: Int,
    onDayClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val color = MaterialTheme.colorScheme.tertiary
        .adjustToBucketIndex(bucketInfo.bucketIndex, bucketCount)

    val todayModifier = if (today == day.date) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    } else Modifier
    Box(
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                text = AnnotatedString(DateTimeFormatter.ofPattern("LL dd").format(day.date))
            }
            .padding(4.dp)
            .then(todayModifier)
            .clip(CircleShape)
            .clickable { onDayClick(day.date) }
            .aspectRatio(1f)
            .background(color, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val isFaded = day.position != DayPosition.MonthDate || day.date.isAfter(today)
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.alpha(if (isFaded) 0.5f else 1f),
        )
    }
}

@Composable
private fun DayPopup(
    habits: ImmutableList<Habit>,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
    ) {
        val shape = MaterialTheme.shapes.small
        Box(
            Modifier
                .shadow(4.dp, shape)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(
                    text = LocalContext.current.resources.getQuantityString(
                        R.plurals.insights_heatmap_popup_action_count,
                        habits.size,
                        habits.size
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Divider(Modifier.padding(bottom = 8.dp, top = 4.dp))
                Text(
                    text = habits.joinToString(separator = "\n") { it.name },
                    style = MaterialTheme.typography.bodySmall
                )
            }

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
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(end = 8.dp).alignByBaseline()
        )

        Row(
            Modifier.border(1.dp, LocalAppColors.current.gray2).alignByBaseline()
        ) {
            heatmapData.bucketMaxValues.forEach {
                val bucketIndex = it.first
                val maxValue = it.second
                val backgroundColor = MaterialTheme.colorScheme.tertiary.adjustToBucketIndex(
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
                        modifier = Modifier.fillMaxSize().padding(top = 5.dp)
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
        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

private fun hasEnoughData(heatmapData: HeatmapMonth): Boolean {
    return heatmapData.totalHabitCount > 0
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

@Preview
@ShowkaseComposable(name = "Heatmap", group = "Insights")
@Composable
fun PreviewHeatmap() {
    PreviewTheme {
        var yearMonth by remember { mutableStateOf(YearMonth.of(2021, 4)) }
        val heatmapState = Result.Success(
            HeatmapMonth(
                yearMonth = yearMonth,
                dayMap = persistentMapOf(
                    LocalDate.of(2021, 4, 20) to HeatmapMonth.BucketInfo(2, 2),
                    LocalDate.of(2021, 4, 21) to HeatmapMonth.BucketInfo(1, 1),
                    LocalDate.of(2021, 4, 22) to HeatmapMonth.BucketInfo(0, 0),
                    LocalDate.of(2021, 4, 23) to HeatmapMonth.BucketInfo(3, 3),
                    LocalDate.of(2021, 4, 24) to HeatmapMonth.BucketInfo(4, 4),
                ),
                totalHabitCount = 4,
                bucketCount = 5,
                bucketMaxValues = persistentListOf(
                    0 to 0,
                    1 to 1,
                    2 to 2,
                    3 to 3,
                    4 to 4
                )
            )
        )

        val onMonthChange: (YearMonth) -> Unit = {
            yearMonth = it
        }

        Heatmap(StableHolder(yearMonth), heatmapState, null, onMonthChange, {})
    }
}