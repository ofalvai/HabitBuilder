package com.ofalvai.habittracker.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = AppColor.primary,
    primaryVariant = AppColor.primaryVariant,
    secondary = AppColor.secondary,
)

private val LightColorPalette = lightColors(
    primary = AppColor.primary,
    primaryVariant = AppColor.primaryVariant,
    secondary = AppColor.secondary,

    background = AppColor.Light.background,
    surface = AppColor.Light.surface,
    onSurface = AppColor.Light.onSurface,
    onBackground = AppColor.Light.onBackground
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