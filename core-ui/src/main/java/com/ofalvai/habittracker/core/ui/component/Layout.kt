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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

@Composable
fun HorizontalGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(content, modifier) { measurables, constraints ->
        val itemConstraints = Constraints.fixedWidth(constraints.maxWidth / measurables.size)
        val placeables = measurables.map {
            it.measure(itemConstraints)
        }

        val height = placeables.maxOf { it.height }
        layout(constraints.maxWidth, height) {
            var xOffset = 0

            placeables.forEach {
                it.placeRelative(x = xOffset, y = 0)
                xOffset += it.width
            }
        }
    }
}