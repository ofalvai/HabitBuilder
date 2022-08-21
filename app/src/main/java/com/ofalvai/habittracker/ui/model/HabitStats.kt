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

package com.ofalvai.habittracker.ui.model

import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import com.ofalvai.habittracker.core.model.HabitId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

@Immutable
data class SingleStats(
    val firstDay: LocalDate?,
    val actionCount: Int,
    val weeklyActionCount: Int,
    @FloatRange(from = 0.0, to = 1.0) val completionRate: Float,
)

@Immutable
data class ActionCountByWeek(
    val year: Year,
    val weekOfYear: Int,
    val actionCount: Int
)

@Immutable
data class ActionCountByMonth(
    val yearMonth: YearMonth,
    val actionCount: Int
)

data class ActionCountChart(
    val items: ImmutableList<ChartItem>,
    val type: Type
) {
    data class ChartItem(
        val label: String,
        val year: Int,
        val value: Int
    )

    enum class Type {
        Weekly, Monthly;

        fun invert() = if (this == Weekly) Monthly else Weekly
    }
}

typealias BucketIndex = Int

@Immutable
data class HeatmapMonth(
    val yearMonth: YearMonth,
    val dayMap: ImmutableMap<LocalDate, BucketInfo>,
    val totalHabitCount: Int,
    val bucketCount: Int,
    val bucketMaxValues: ImmutableList<Pair<BucketIndex, Int>>
) {
    data class BucketInfo(
        val bucketIndex: BucketIndex,
        val value: Int // Actual value (habit count on day) that bucketing is based on
    )
}

data class TopHabitItem(
    val habitId: HabitId,
    val name: String,
    val count: Int,
    @FloatRange(from = 0.0, to = 1.0) val progress: Float
)

data class TopDayItem(
    val habitId: HabitId,
    val name: String,
    val dayLabel: String,
    val count: Int
)