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

package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.mapper.mapActionCountByWeekListToItemList
import com.ofalvai.habittracker.ui.model.ActionCountByWeek
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import java.time.LocalDate
import java.time.Year
import kotlin.math.max

private const val MinBarHeight = 0.001f
private val BarWidth = 32.dp

data class ChartItem(
    val label: String,
    val year: Int,
    val value: Int
)

@Composable
fun ActionCountChart(
    values: List<ChartItem>,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) {
        return
    }
    val maxValue = values.maxByOrNull { it.value }!!.value
    // Reverse items and use reverseLayout = true to initially scroll to end
    val reversedItems = remember(values) { values.reversed() }


    LazyRow(
        modifier = modifier.height(200.dp),
        reverseLayout = true
    ) {
        itemsIndexed(reversedItems) { index, chartItem ->
            Column(
                modifier = Modifier.padding(horizontal = 4.dp).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom,
                    ) {
                    val value = chartItem.value
                    val heightRatio = if (maxValue > 0) value / maxValue.toFloat() else 0f
                    val isEven = index % 2 == 0
                    Text(
                        modifier = Modifier.width(BarWidth).padding(top = 8.dp),
                        text = value.toString(),
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .background(if (isEven) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = .8f))
                            .fillMaxHeight(fraction = max(MinBarHeight, heightRatio))
                            .width(BarWidth)
                    )
                }
                Text(
                    modifier = Modifier.width(BarWidth),
                    text = chartItem.label,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
fun PreviewActionCountChart() {
    HabitTrackerTheme {
        val actionCounts = listOf(
            ActionCountByWeek(Year.of(2021), 1, 3),
            ActionCountByWeek(Year.of(2021), 2, 4),
            ActionCountByWeek(Year.of(2021), 3, 10),
            ActionCountByWeek(Year.of(2021), 4, 0),
            ActionCountByWeek(Year.of(2021), 5, 0),
            ActionCountByWeek(Year.of(2021), 6, 2),
            ActionCountByWeek(Year.of(2021), 7, 2),
            ActionCountByWeek(Year.of(2021), 8, 5),
            ActionCountByWeek(Year.of(2021), 9, 7),
            ActionCountByWeek(Year.of(2021), 10, 8),
            ActionCountByWeek(Year.of(2021), 11, 9),
            ActionCountByWeek(Year.of(2021), 12, 7)
        )
        ActionCountChart(mapActionCountByWeekListToItemList(actionCounts, today = LocalDate.now()))
    }
}