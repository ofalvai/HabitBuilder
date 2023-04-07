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

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

val LightColors = lightColorScheme(
    primary = MaterialPalette.md_theme_light_primary,
    onPrimary = MaterialPalette.md_theme_light_onPrimary,
    primaryContainer = MaterialPalette.md_theme_light_primaryContainer,
    onPrimaryContainer = MaterialPalette.md_theme_light_onPrimaryContainer,
    secondary = MaterialPalette.md_theme_light_secondary,
    onSecondary = MaterialPalette.md_theme_light_onSecondary,
    secondaryContainer = MaterialPalette.md_theme_light_secondaryContainer,
    onSecondaryContainer = MaterialPalette.md_theme_light_onSecondaryContainer,
    tertiary = MaterialPalette.md_theme_light_tertiary,
    onTertiary = MaterialPalette.md_theme_light_onTertiary,
    tertiaryContainer = MaterialPalette.md_theme_light_tertiaryContainer,
    onTertiaryContainer = MaterialPalette.md_theme_light_onTertiaryContainer,
    error = MaterialPalette.md_theme_light_error,
    errorContainer = MaterialPalette.md_theme_light_errorContainer,
    onError = MaterialPalette.md_theme_light_onError,
    onErrorContainer = MaterialPalette.md_theme_light_onErrorContainer,
    background = MaterialPalette.md_theme_light_background,
    onBackground = MaterialPalette.md_theme_light_onBackground,
    surface = MaterialPalette.md_theme_light_surface,
    onSurface = MaterialPalette.md_theme_light_onSurface,
    surfaceVariant = MaterialPalette.md_theme_light_surfaceVariant,
    onSurfaceVariant = MaterialPalette.md_theme_light_onSurfaceVariant,
    outline = MaterialPalette.md_theme_light_outline,
    inverseOnSurface = MaterialPalette.md_theme_light_inverseOnSurface,
    inverseSurface = MaterialPalette.md_theme_light_inverseSurface,
    inversePrimary = MaterialPalette.md_theme_light_inversePrimary,
    surfaceTint = MaterialPalette.md_theme_light_surfaceTint,
    outlineVariant = MaterialPalette.md_theme_light_outlineVariant,
    scrim = MaterialPalette.md_theme_light_scrim,
)

val DarkColors = darkColorScheme(
    primary = MaterialPalette.md_theme_dark_primary,
    onPrimary = MaterialPalette.md_theme_dark_onPrimary,
    primaryContainer = MaterialPalette.md_theme_dark_primaryContainer,
    onPrimaryContainer = MaterialPalette.md_theme_dark_onPrimaryContainer,
    secondary = MaterialPalette.md_theme_dark_secondary,
    onSecondary = MaterialPalette.md_theme_dark_onSecondary,
    secondaryContainer = MaterialPalette.md_theme_dark_secondaryContainer,
    onSecondaryContainer = MaterialPalette.md_theme_dark_onSecondaryContainer,
    tertiary = MaterialPalette.md_theme_dark_tertiary,
    onTertiary = MaterialPalette.md_theme_dark_onTertiary,
    tertiaryContainer = MaterialPalette.md_theme_dark_tertiaryContainer,
    onTertiaryContainer = MaterialPalette.md_theme_dark_onTertiaryContainer,
    error = MaterialPalette.md_theme_dark_error,
    errorContainer = MaterialPalette.md_theme_dark_errorContainer,
    onError = MaterialPalette.md_theme_dark_onError,
    onErrorContainer = MaterialPalette.md_theme_dark_onErrorContainer,
    background = MaterialPalette.md_theme_dark_background,
    onBackground = MaterialPalette.md_theme_dark_onBackground,
    surface = MaterialPalette.md_theme_dark_surface,
    onSurface = MaterialPalette.md_theme_dark_onSurface,
    surfaceVariant = MaterialPalette.md_theme_dark_surfaceVariant,
    onSurfaceVariant = MaterialPalette.md_theme_dark_onSurfaceVariant,
    outline = MaterialPalette.md_theme_dark_outline,
    inverseOnSurface = MaterialPalette.md_theme_dark_inverseOnSurface,
    inverseSurface = MaterialPalette.md_theme_dark_inverseSurface,
    inversePrimary = MaterialPalette.md_theme_dark_inversePrimary,
    surfaceTint = MaterialPalette.md_theme_dark_surfaceTint,
    outlineVariant = MaterialPalette.md_theme_dark_outlineVariant,
    scrim = MaterialPalette.md_theme_dark_scrim,
)

val LightAppColors = AppColors(
    successContainer = AppPalette.Light.successContainer,
    gray1 = AppPalette.Light.gray1,
    gray2 = AppPalette.Light.gray2,
    habitRed = AppPalette.light_HabitRed,
    habitRedContainer = AppPalette.light_HabitRedContainer,
    onHabitRedContainer = AppPalette.light_onHabitRedContainer,
    habitGreen = AppPalette.light_HabitGreen,
    habitGreenContainer = AppPalette.light_HabitGreenContainer,
    onHabitGreenContainer = AppPalette.light_onHabitGreenContainer,
    habitBlue = AppPalette.light_HabitBlue,
    habitBlueContainer = AppPalette.light_HabitBlueContainer,
    onHabitBlueContainer = AppPalette.light_onHabitBlueContainer,
    habitYellow = AppPalette.light_HabitYellow,
    habitYellowContainer = AppPalette.light_HabitYellowContainer,
    onHabitYellowContainer = AppPalette.light_onHabitYellowContainer,
    habitCyan = AppPalette.light_HabitCyan,
    habitCyanContainer = AppPalette.light_HabitCyanContainer,
    onHabitCyanContainer = AppPalette.light_onHabitCyanContainer,
    habitPink = AppPalette.light_HabitPink,
    habitPinkContainer = AppPalette.light_HabitPinkContainer,
    onHabitPinkContainer = AppPalette.light_onHabitPinkContainer,
)

val DarkAppColors = AppColors(
    successContainer = AppPalette.Dark.successContainer,
    gray1 = AppPalette.Dark.gray1,
    gray2 = AppPalette.Dark.gray2,
    habitRed = AppPalette.dark_HabitRed,
    habitRedContainer = AppPalette.dark_HabitRedContainer,
    onHabitRedContainer = AppPalette.dark_onHabitRedContainer,
    habitGreen = AppPalette.dark_HabitGreen,
    habitGreenContainer = AppPalette.dark_HabitGreenContainer,
    onHabitGreenContainer = AppPalette.dark_onHabitGreenContainer,
    habitBlue = AppPalette.dark_HabitBlue,
    habitBlueContainer = AppPalette.dark_HabitBlueContainer,
    onHabitBlueContainer = AppPalette.dark_onHabitBlueContainer,
    habitYellow = AppPalette.dark_HabitYellow,
    habitYellowContainer = AppPalette.dark_HabitYellowContainer,
    onHabitYellowContainer = AppPalette.dark_onHabitYellowContainer,
    habitCyan = AppPalette.dark_HabitCyan,
    habitCyanContainer = AppPalette.dark_HabitCyanContainer,
    onHabitCyanContainer = AppPalette.dark_onHabitCyanContainer,
    habitPink = AppPalette.dark_HabitPink,
    habitPinkContainer = AppPalette.dark_HabitPinkContainer,
    onHabitPinkContainer = AppPalette.dark_onHabitPinkContainer,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

private val shapes = Shapes()

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isDynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val dynamicColor = isDynamicColor && isDynamicThemeAvailable()
    val colorScheme = when {
        dynamicColor && isDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        dynamicColor && !isDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        isDarkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography
    ) {
        val appColors = if (isSystemInDarkTheme()) DarkAppColors else LightAppColors
        CompositionLocalProvider(LocalAppColors provides appColors) {
            content()
        }
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isDynamicThemeAvailable() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun PreviewTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        typography = typography,
        shapes = shapes,
        content = {
            // Draw a real background around content
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                // Workaround for incorrect colors in Showkase dark mode preview
                val onBackgroundColor = if (isDark) {
                    DarkColors.onBackground
                } else {
                    LightColors.onBackground
                }
                val appColors = if (isSystemInDarkTheme()) DarkAppColors else LightAppColors
                CompositionLocalProvider(
                    LocalContentColor provides onBackgroundColor,
                    LocalAppColors provides appColors
                ) {
                    content()
                }
            }
        }
    )
}

