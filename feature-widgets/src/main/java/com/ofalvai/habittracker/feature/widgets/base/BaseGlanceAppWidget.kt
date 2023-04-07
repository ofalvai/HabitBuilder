/*
 * Copyright 2023 Olivér Falvai
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

package com.ofalvai.habittracker.feature.widgets.base

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.glance.GlanceId
import androidx.glance.LocalGlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// Credit: https://github.com/joreilly/PeopleInSpace/blob/fdea8b6177c646a48833976104dbb973cfbbb60f/app/src/main/java/com/surrus/peopleinspace/glance/util/BaseGlanceAppWidget.kt
abstract class BaseGlanceAppWidget<T>(
    initialData: T,
    private val app: Application
) : GlanceAppWidget() {

    private var glanceId by mutableStateOf<GlanceId?>(null)
    private var size by mutableStateOf<DpSize?>(null)
    private var data by mutableStateOf<T>(initialData)

    private val coroutineScope = MainScope()

    abstract suspend fun loadData(): T

    fun initiateLoad() {
        coroutineScope.launch {
            data = loadData()

            val currentGlanceId = snapshotFlow { glanceId }.filterNotNull().firstOrNull()

            if (currentGlanceId != null) {
                update(app.baseContext, currentGlanceId)
            }
        }
    }

    @Composable
    override fun Content() {
        glanceId = LocalGlanceId.current
        size = LocalSize.current

        Content(data)
    }

    @Composable
    abstract fun Content(data: T)
}