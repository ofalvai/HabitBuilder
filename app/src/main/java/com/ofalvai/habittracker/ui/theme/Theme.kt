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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorPalette = lightColors(
    primary = AppColor.Light.primary,
    primaryVariant = AppColor.Light.primaryVariant,
    secondary = AppColor.Light.secondary,

    background = AppColor.Light.background,
    surface = AppColor.Light.surface,

    onPrimary = AppColor.Light.onPrimary,
    onSecondary = AppColor.Light.onSecondary,
    onSurface = AppColor.Light.onSurface,
    onBackground = AppColor.Light.onBackground
)

private val DarkColorPalette = darkColors(
    primary = AppColor.Dark.primary,
    primaryVariant = AppColor.Dark.primaryVariant,
    secondary = AppColor.Dark.secondary,

    background = AppColor.Dark.background,
    surface = AppColor.Dark.surface,

    onPrimary = AppColor.Dark.onPrimary,
    onSecondary = AppColor.Dark.onSecondary,
    onSurface = AppColor.Dark.onSurface,
    onBackground = AppColor.Dark.onBackground
)

val shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(0.dp)
)

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}