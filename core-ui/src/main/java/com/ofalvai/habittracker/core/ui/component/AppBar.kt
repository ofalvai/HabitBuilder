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

import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.core.ui.R


@Composable
fun AppBar(
    title: @Composable () -> Unit,
    iconActions: @Composable RowScope.() -> Unit = {},
    dropdownMenuItems: @Composable ColumnScope.() -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(Modifier.height(48.dp).fillMaxWidth()) {
        Box(Modifier.align(Alignment.Center)) {
            title()
        }

        Row(Modifier.align(Alignment.CenterEnd)) {
            iconActions()

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Rounded.MoreVert, stringResource(R.string.common_more))
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    offset = DpOffset(8.dp, 0.dp)
                ) {
                    dropdownMenuItems()
                }
            }
        }
    }
}