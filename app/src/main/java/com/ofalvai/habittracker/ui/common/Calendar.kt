/*
 * Copyright 2021 Olivér Falvai
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

package com.ofalvai.habittracker.ui.common

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import java.time.Year
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

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
private fun PreviewCalendarPager() {
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
private fun PreviewCalendarDayLegend() {
    HabitTrackerTheme {
        CalendarDayLegend(WeekFields.ISO)
    }
}