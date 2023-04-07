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

package com.ofalvai.habittracker.feature.widgets

import android.content.res.Configuration
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.glance.GlanceComposable
import androidx.glance.LocalContext
import com.ofalvai.habittracker.core.ui.theme.DarkAppColors
import com.ofalvai.habittracker.core.ui.theme.DarkColors
import com.ofalvai.habittracker.core.ui.theme.LightAppColors
import com.ofalvai.habittracker.core.ui.theme.LightColors
import com.ofalvai.habittracker.core.ui.theme.LocalAppColors
import com.ofalvai.habittracker.core.ui.theme.isDynamicThemeAvailable

@GlanceComposable
@Composable
fun GlanceTheme(content: @Composable () -> Unit) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isDynamicThemeAvailable = isDynamicThemeAvailable()
    val colorScheme = when {
        isDynamicThemeAvailable && isSystemInDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        isDynamicThemeAvailable && !isSystemInDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        isSystemInDarkTheme -> DarkColors
        else -> LightColors
    }

    val appColors = if (isSystemInDarkTheme()) DarkAppColors else LightAppColors

    CompositionLocalProvider(
        LocalGlanceMaterialColors provides colorScheme,
        LocalAppColors provides appColors,
    ) {
        content()
    }
}

object GlanceTheme {
    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalGlanceMaterialColors.current
}

val LocalGlanceMaterialColors = staticCompositionLocalOf { LightColors }

@Composable
@ReadOnlyComposable
private fun isSystemInDarkTheme(): Boolean {
    // TODO: observe changes to configuration
    val uiMode = LocalContext.current.resources.configuration.uiMode
    return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}