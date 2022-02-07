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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val LightColorPalette = lightColors(
    primary = AppColor.Light.primary,
    primaryVariant = AppColor.Light.primaryVariant,
    secondary = AppColor.Light.secondary,
    secondaryVariant = AppColor.Light.secondary,

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
    secondaryVariant = AppColor.Dark.secondary,

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
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) DarkColorPalette else LightColorPalette,
        typography = typography,
        shapes = shapes,
        content = {
            Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                content()
            }
        }
    )
}

@Composable
fun PreviewTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colors = if (isDark) DarkColorPalette else LightColorPalette,
        typography = typography,
        shapes = shapes,
        content = {
            // Draw a real background around content
            Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                // Workaround for incorrect colors in Showkase dark mode preview
                val onBackgroundColor = if (isDark) {
                    DarkColorPalette.onBackground
                } else {
                    LightColorPalette.onBackground
                }
                CompositionLocalProvider(LocalContentColor provides onBackgroundColor) {
                    content()
                }
            }
        }
    )
}

@Composable
fun PreviewTheme2(content: @Composable () -> Unit) {

    val baseConfiguration = LocalContext.current.resources.configuration
    val darkModeConfig = Configuration(baseConfiguration).apply {
        uiMode = Configuration.UI_MODE_NIGHT_YES
    }

    Column {
        MaterialTheme(
            colors = if (isSystemInDarkTheme()) DarkColorPalette else LightColorPalette,
            typography = typography,
            shapes = shapes,
            content = {
                // Draw a real background around content
                Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                    content()
                }
            }
        )

        CompositionLocalProvider(LocalConfiguration provides darkModeConfig) {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) DarkColorPalette else LightColorPalette,
                typography = typography,
                shapes = shapes,
                content = {
                    // Draw a real background around content
                    Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                        content()
                    }
                }
            )
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) DarkColorPalette else LightColorPalette,
                typography = typography,
                shapes = shapes,
                content = {
                    // Draw a real background around content
                    Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                        content()
                    }
                }
            )
        }

        val baseDensity = LocalDensity.current
        val fontScaledDensity = Density(fontScale = baseDensity.fontScale * 2, density = baseDensity.density)
        CompositionLocalProvider(LocalDensity provides fontScaledDensity) {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) DarkColorPalette else LightColorPalette,
                typography = typography,
                shapes = shapes,
                content = {
                    // Draw a real background around content
                    Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                        content()
                    }
                }
            )
        }

        val scaledDensity = Density(density = baseDensity.density * 2f)
        CompositionLocalProvider(LocalDensity provides scaledDensity) {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) DarkColorPalette else LightColorPalette,
                typography = typography,
                shapes = shapes,
                content = {
                    // Draw a real background around content
                    Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                        content()
                    }
                }
            )
        }
    }



}

