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

package com.ofalvai.habittracker.feature.dashboard.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import java.time.Year
import java.time.YearMonth

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