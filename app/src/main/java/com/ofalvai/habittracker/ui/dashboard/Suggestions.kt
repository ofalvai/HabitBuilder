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

package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ofalvai.habittracker.R

object Suggestions {

    val habits: List<String>
        @Composable get() = listOf(
            stringResource(R.string.habit_suggestion_reading),
            stringResource(R.string.habit_suggestion_workout),
            stringResource(R.string.habit_suggestion_meditation),
            stringResource(R.string.habit_suggestion_walk),
            stringResource(R.string.habit_suggestion_plan_my_day),
            stringResource(R.string.habit_suggestion_stretch),
            stringResource(R.string.habit_suggestion_go_to_bed),
            stringResource(R.string.habit_suggestion_journal),
            stringResource(R.string.habit_suggestion_spend_time),
        )
}