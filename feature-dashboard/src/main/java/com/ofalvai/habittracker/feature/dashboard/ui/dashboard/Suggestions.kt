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

package com.ofalvai.habittracker.feature.dashboard.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.ofalvai.habittracker.core.common.R as commonR

object Suggestions {

    val habits: ImmutableList<String>
        @Composable get() = persistentListOf(
            stringResource(commonR.string.habit_suggestion_reading),
            stringResource(commonR.string.habit_suggestion_workout),
            stringResource(commonR.string.habit_suggestion_meditation),
            stringResource(commonR.string.habit_suggestion_walk),
            stringResource(commonR.string.habit_suggestion_plan_my_day),
            stringResource(commonR.string.habit_suggestion_stretch),
            stringResource(commonR.string.habit_suggestion_go_to_bed),
            stringResource(commonR.string.habit_suggestion_journal),
            stringResource(commonR.string.habit_suggestion_spend_time),
        )
}