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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.ofalvai.habittracker.ui.model.Habit

object AppColor {

    object Light {
        val primary = Color(0xFF7B5800)
        val primaryVariant = Color(0xFF6C5C3F)
        val secondary = Color(0xFF4B6545)
        val background = Color(0xFFFFF5E5)
        val surface = Color(0xFFFFFBF8)
        val surfaceVariant = Color(0xFFEEE1CF)

        val onPrimary = Color.White
        val onSecondary = Color.White
        val onSurface = Color(0xFF1E1B16)
        val onBackground = Color(0xFF1E1B16)

        val gray1 = Color.Black.copy(alpha = 0.1f)
        val gray2 = Color.Black.copy(alpha = 0.25f)

        val habitRed = Color(0xFF9C4043)
        val habitGreen = Color(0xFF008768)
        val habitBlue = Color(0xFF1E5FA6)
        val habitYellow = Color(0xFF7D5800)
        val habitCyan = Color(0xFF00696D)
        val habitPink = Color(0xFFA400B5)
    }

    object Dark {
        val primary = Color(0xFFF5BE48)
        val primaryVariant = Color(0xFF412D00)
        val secondary = Color(0xFF4B6545)
        val background = Color(0xFF1E1B16)
        val surface = Color(0xFF1E1B16)

        val onPrimary = Color(0xFF412D00)
        val onSecondary = Color(0xFF1F361B)
        val onBackground = Color(0xFFE9E1D8)
        val onSurface = Color(0xFFE9E1D8)
        val surfaceVariant = Color(0xFF4E4639)

        val gray1 = Color.White.copy(alpha = 0.1f)
        val gray2 = Color.White.copy(alpha = 0.25f)

        val habitRed = Color(0xFFFFB3B2)
        val habitGreen = Color(0xFF63DBB6)
        val habitBlue = Color(0xFFA4C8FF)
        val habitYellow = Color(0xFFF7BD48)
        val habitCyan = Color(0xFF02DCE3)
        val habitPink = Color(0xFFFFA8FF)
    }
}

/**
 * Background for surfaces that need to distinct from the background, but don't use elevation.
 * It's a single color in both light and dark mode (higher elevation won't make it lighter)
 */
val Colors.surfaceVariant: Color
    get() = if (isLight) AppColor.Light.surfaceVariant else AppColor.Dark.surfaceVariant

val Colors.gray1: Color
    get() = if (isLight) AppColor.Light.gray1 else AppColor.Dark.gray1

val Colors.gray2: Color
    get() = if (isLight) AppColor.Light.gray2 else AppColor.Dark.gray2

val Colors.habitRed: Color
    get() = if (isLight) AppColor.Light.habitRed else AppColor.Dark.habitRed

val Colors.habitGreen: Color
    get() = if (isLight) AppColor.Light.habitGreen else AppColor.Dark.habitGreen

val Colors.habitBlue: Color
    get() = if (isLight) AppColor.Light.habitBlue else AppColor.Dark.habitBlue

val Colors.habitYellow: Color
    get() = if (isLight) AppColor.Light.habitYellow else AppColor.Dark.habitYellow

val Colors.habitCyan: Color
    get() = if (isLight) AppColor.Light.habitCyan else AppColor.Dark.habitCyan

val Colors.habitPink: Color
    get() = if (isLight) AppColor.Light.habitPink else AppColor.Dark.habitPink


val Habit.Color.composeColor: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        Habit.Color.Green -> MaterialTheme.colors.habitGreen
        Habit.Color.Blue -> MaterialTheme.colors.habitBlue
        Habit.Color.Yellow -> MaterialTheme.colors.habitYellow
        Habit.Color.Red -> MaterialTheme.colors.habitRed
        Habit.Color.Cyan -> MaterialTheme.colors.habitCyan
        Habit.Color.Pink -> MaterialTheme.colors.habitPink
    }