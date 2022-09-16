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

package com.ofalvai.habittracker.feature.insights.model

import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import com.ofalvai.habittracker.core.model.HabitId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import java.time.LocalDate
import java.time.YearMonth

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