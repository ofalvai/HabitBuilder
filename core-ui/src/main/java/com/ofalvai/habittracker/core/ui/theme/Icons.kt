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

package com.ofalvai.habittracker.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.ofalvai.habittracker.core.ui.R

object CoreIcons {

    val Habits: Painter
        @Composable
        get() = painterResource(R.drawable.ic_habit)

    val Error: Painter
        @Composable
        get() = painterResource(R.drawable.ic_error)

    val Archive: Painter
        @Composable
        get() = painterResource(R.drawable.ic_archive)

    val ChevronLeft: Painter
        @Composable
        get() = painterResource(R.drawable.ic_chevron_left)

    val ChevronRight: Painter
        @Composable
        get() = painterResource(R.drawable.ic_chevron_right)

    val Settings: Painter
        @Composable
        get() = painterResource(R.drawable.ic_settings)

    val Export: Painter
        @Composable
        get() = painterResource(R.drawable.ic_export)

    val InfoOutlined: Painter
        @Composable
        get() = painterResource(R.drawable.ic_info_outlined)
}

