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

package com.ofalvai.habittracker.core.ui.semantics

import android.text.format.DateUtils
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import com.ofalvai.habittracker.core.model.Action
import com.ofalvai.habittracker.core.ui.R

fun Modifier.habitActionSemantics(action: Action) = composed {
    val state = stringResource(
        if (action.toggled) R.string.action_state_toggled else R.string.action_state_not_toggled
    )
    val timestampLabel = if (action.timestamp != null) {
        DateUtils.getRelativeDateTimeString(
            LocalContext.current,
            action.timestamp!!.toEpochMilli(),
            DateUtils.DAY_IN_MILLIS,
            DateUtils.WEEK_IN_MILLIS,
            0
        ).toString()
    } else stringResource(R.string.action_text_no_date)

    semantics(mergeDescendants = true) {
        stateDescription = state
        text = AnnotatedString.Builder(timestampLabel).toAnnotatedString()
    }
}