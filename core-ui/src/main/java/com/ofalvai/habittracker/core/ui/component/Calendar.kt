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

package com.ofalvai.habittracker.core.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.ui.R
import com.ofalvai.habittracker.core.ui.theme.CoreIcons
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import java.time.Year
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarPager(
    yearMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(
                CoreIcons.ChevronLeft,
                contentDescription = stringResource(R.string.calendar_previous_month)
            )
        }

        AnimatedContent(
            targetState = yearMonth,
            transitionSpec = pagerTransitionSpec,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) { targetYearMonth ->
            val month = targetYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val year = targetYearMonth.year
            val label = if (year == Year.now().value) month else "$month $year"
            Text(
                text = label,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        IconButton(onClick = onNextClick) {
            Icon(
                CoreIcons.ChevronRight,
                contentDescription = stringResource(R.string.calendar_next_month)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private val pagerTransitionSpec: AnimatedContentScope<YearMonth>.() -> ContentTransform = {
    if (targetState.isAfter(initialState)) {
        // Slide in from right, slide out to left
        slideInHorizontally { it } + fadeIn() with slideOutHorizontally { -it } + fadeOut()
    } else {
        // Slide in from left, slide out to right
        slideInHorizontally { -it } + fadeIn() with slideOutHorizontally { it } + fadeOut()
    }
}

@Composable
fun CalendarDayLegend() {
    val weekFields = WeekFields.of(Locale.getDefault())
    HorizontalGrid(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        (0..6).map {
            val day = weekFields.firstDayOfWeek.plus(it.toLong())
            val label = day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            Text(
                text = label,
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@ShowkaseComposable(name = "Pager", group = "Calendar")
@Composable
fun PreviewCalendarPager() {
    PreviewTheme {
        var yearMonth by remember { mutableStateOf(YearMonth.of(2022, 1)) }
        CalendarPager(
            yearMonth = yearMonth,
            onPreviousClick = { yearMonth = yearMonth.minusMonths(1) },
            onNextClick = { yearMonth = yearMonth.plusMonths(1) }
        )
    }
}

@Preview
@ShowkaseComposable(name = "Day legend", group = "Calendar")
@Composable
fun PreviewCalendarDayLegend() {
    PreviewTheme {
        CalendarDayLegend()
    }
}