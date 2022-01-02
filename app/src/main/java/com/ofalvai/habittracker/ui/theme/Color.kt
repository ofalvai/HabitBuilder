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

package com.ofalvai.habittracker.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ofalvai.habittracker.ui.model.Habit

object AppColor {

    val primary = Color(0xFF2B3940)
    val primaryVariant = Color(0xFFFFF5E5)
    val secondary = Color.Cyan // TODO

    object Light {
        val background = Color(0xFFFFF1DD)
        val surface = Color.White
        val onSurface = Color(0xFF262626)
        val onBackground = Color(0xFF262626)

        val surfaceVariant = Color(0xFFFFF8ED)
        val gray1 = Color.Black.copy(alpha = 0.1f)
        val gray2 = Color.Black.copy(alpha = 0.25f)

        val habitRed = Color(0xFFE08F8F)
        val habitGreen = Color(0xFFAEC2B8)
        val habitBlue = Color(0xFF9EB2D1)
        val habitYellow = Color(0xFFFFCC79)
        val habitPink = Color(0xFFE08FB8)
    }

    object Dark {
        val surfaceVariant = Color(0xFF232323)
        val gray1 = Color.White.copy(alpha = 0.1f)
        val gray2 = Color.White.copy(alpha = 0.25f)

        val habitRed = Color(0xFFE08F8F)
        val habitGreen = Color(0xFFAEC2B8)
        val habitBlue = Color(0xFF9EB2D1)
        val habitYellow = Color(0xFFFFCC79)
        val habitPink = Color(0xFFE08FB8)
    }
}

/**
 * Background for surfaces that need to distinct from the background, but don't use elevation.
 * It's a single color in both light and dark mode (higher elevation won't make it lighter)
 */
val Colors.surfaceVariant: Color
    @Composable
    get() = if (isLight) AppColor.Light.surfaceVariant else AppColor.Dark.surfaceVariant

val Colors.gray1: Color
    @Composable
    get() = if (isLight) AppColor.Light.gray1 else AppColor.Dark.gray1

val Colors.gray2: Color
    @Composable
    get() = if (isLight) AppColor.Light.gray2 else AppColor.Dark.gray2

val Colors.habitRed: Color
    @Composable
    get() = if (isLight) AppColor.Light.habitRed else AppColor.Dark.habitRed

val Colors.habitGreen: Color
    @Composable
    get() = if (isLight) AppColor.Light.habitGreen else AppColor.Dark.habitGreen

val Colors.habitBlue: Color
    @Composable
    get() = if (isLight) AppColor.Light.habitBlue else AppColor.Dark.habitBlue

val Colors.habitYellow: Color
    @Composable
    get() = if (isLight) AppColor.Light.habitYellow else AppColor.Dark.habitYellow


val Habit.Color.composeColor: Color
    @Composable
    get() = when (this) {
        Habit.Color.Green -> MaterialTheme.colors.habitGreen
        Habit.Color.Blue -> MaterialTheme.colors.habitBlue
        Habit.Color.Yellow -> MaterialTheme.colors.habitYellow
        Habit.Color.Red -> MaterialTheme.colors.habitRed
    }