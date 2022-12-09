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

import androidx.compose.ui.graphics.Color

/**
 * Extended colors in addition to [MaterialPalette]
 */
internal object AppPalette {
    val light_HabitBlue = Color(0xff006495)
    val light_HabitBlueContainer = Color(0xffcbe6ff)
    val light_onHabitBlueContainer = Color(0xff001e30)
    val dark_HabitBlue = Color(0xff8fcdff)
    val dark_HabitBlueContainer = Color(0xff004b71)
    val dark_onHabitBlueContainer = Color(0xffcbe6ff)
    val light_HabitRed = Color(0xffa14000)
    val light_HabitRedContainer = Color(0xffffdbcc)
    val light_onHabitRedContainer = Color(0xff351000)
    val dark_HabitRed = Color(0xffffb693)
    val dark_HabitRedContainer = Color(0xff7a2f00)
    val dark_onHabitRedContainer = Color(0xffffdbcc)
    val light_HabitGreen = Color(0xff006d3c)
    val light_HabitGreenContainer = Color(0xff98f7b5)
    val light_onHabitGreenContainer = Color(0xff00210e)
    val dark_HabitGreen = Color(0xff7cda9b)
    val dark_HabitGreenContainer = Color(0xff00522b)
    val dark_onHabitGreenContainer = Color(0xff98f7b5)
    val light_HabitYellow = Color(0xff7c5800)
    val light_HabitYellowContainer = Color(0xffffdea7)
    val light_onHabitYellowContainer = Color(0xff271900)
    val dark_HabitYellow = Color(0xfff7bd48)
    val dark_HabitYellowContainer = Color(0xff5e4200)
    val dark_onHabitYellowContainer = Color(0xffffdea7)
    val light_HabitCyan = Color(0xff006a60)
    val light_HabitCyanContainer = Color(0xff74f8e5)
    val light_onHabitCyanContainer = Color(0xff00201c)
    val dark_HabitCyan = Color(0xff53dbc9)
    val dark_HabitCyanContainer = Color(0xff005048)
    val dark_onHabitCyanContainer = Color(0xff74f8e5)
    val light_HabitPink = Color(0xffb1008c)
    val light_HabitPinkContainer = Color(0xffffd8ec)
    val light_onHabitPinkContainer = Color(0xff3b002d)
    val dark_HabitPink = Color(0xffffaede)
    val dark_HabitPinkContainer = Color(0xff87006a)
    val dark_onHabitPinkContainer = Color(0xffffd8ec)

    object Light {
        // TODO: replace with neutral tonal palette items
        val gray1 = Color.Black.copy(alpha = 0.1f)
        val gray2 = Color.Black.copy(alpha = 0.25f)

        val successContainer = Color(0xFFCEEBC2)
    }

    object Dark {
        val gray1 = Color.White.copy(alpha = 0.1f)
        val gray2 = Color.White.copy(alpha = 0.25f)

        val successContainer = Color(0xFF354D2F)
    }
}

internal object MaterialPalette {
    val md_theme_light_primary = Color(0xFF7B5800)
    val md_theme_light_onPrimary = Color(0xFFFFFFFF)
    val md_theme_light_primaryContainer = Color(0xFFFFDEA6)
    val md_theme_light_onPrimaryContainer = Color(0xFF271900)
    val md_theme_light_secondary = Color(0xFF6C5C3F)
    val md_theme_light_onSecondary = Color(0xFFFFFFFF)
    val md_theme_light_secondaryContainer = Color(0xFFF6E0BB)
    val md_theme_light_onSecondaryContainer = Color(0xFF251A04)
    val md_theme_light_tertiary = Color(0xFF4C6545)
    val md_theme_light_onTertiary = Color(0xFFFFFFFF)
    val md_theme_light_tertiaryContainer = Color(0xFFCEEBC2)
    val md_theme_light_onTertiaryContainer = Color(0xFF0A2007)
    val md_theme_light_error = Color(0xFFBA1A1A)
    val md_theme_light_errorContainer = Color(0xFFFFDAD6)
    val md_theme_light_onError = Color(0xFFFFFFFF)
    val md_theme_light_onErrorContainer = Color(0xFF410002)
    val md_theme_light_background = Color(0xFFFFF8F3) // MODIFIED: Neutral 98
    val md_theme_light_onBackground = Color(0xFF1E1B16)
    val md_theme_light_surface = Color(0xFFFFF8F3) // MODIFIED: Neutral 98
    val md_theme_light_onSurface = Color(0xFF1E1B16)
    val md_theme_light_surfaceVariant = Color(0xFFEEE1CF)
    val md_theme_light_onSurfaceVariant = Color(0xFF4E4639)
    val md_theme_light_outline = Color(0xFF807667)
    val md_theme_light_inverseOnSurface = Color(0xFFF8EFE7)
    val md_theme_light_inverseSurface = Color(0xFF34302A)
    val md_theme_light_inversePrimary = Color(0xFFF6BD48)
    val md_theme_light_surfaceTint = Color(0xFF7B5800)
    val md_theme_light_outlineVariant = Color(0xFFD1C5B4)
    val md_theme_light_scrim = Color(0xFF000000)

    val md_theme_dark_primary = Color(0xFFF6BD48)
    val md_theme_dark_onPrimary = Color(0xFF412D00)
    val md_theme_dark_primaryContainer = Color(0xFF5D4200)
    val md_theme_dark_onPrimaryContainer = Color(0xFFFFDEA6)
    val md_theme_dark_secondary = Color(0xFFD9C4A0)
    val md_theme_dark_onSecondary = Color(0xFF3C2E15)
    val md_theme_dark_secondaryContainer = Color(0xFF53452A)
    val md_theme_dark_onSecondaryContainer = Color(0xFFF6E0BB)
    val md_theme_dark_tertiary = Color(0xFFB2CFA7)
    val md_theme_dark_onTertiary = Color(0xFF1F361A)
    val md_theme_dark_tertiaryContainer = Color(0xFF354D2F)
    val md_theme_dark_onTertiaryContainer = Color(0xFFCEEBC2)
    val md_theme_dark_error = Color(0xFFFFB4AB)
    val md_theme_dark_errorContainer = Color(0xFF93000A)
    val md_theme_dark_onError = Color(0xFF690005)
    val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
    val md_theme_dark_background = Color(0xFF1E1B16)
    val md_theme_dark_onBackground = Color(0xFFE9E1D9)
    val md_theme_dark_surface = Color(0xFF1E1B16)
    val md_theme_dark_onSurface = Color(0xFFE9E1D9)
    val md_theme_dark_surfaceVariant = Color(0xFF4E4639)
    val md_theme_dark_onSurfaceVariant = Color(0xFFD1C5B4)
    val md_theme_dark_outline = Color(0xFF9A8F80)
    val md_theme_dark_inverseOnSurface = Color(0xFF1E1B16)
    val md_theme_dark_inverseSurface = Color(0xFFE9E1D9)
    val md_theme_dark_inversePrimary = Color(0xFF7B5800)
    val md_theme_dark_surfaceTint = Color(0xFFF6BD48)
    val md_theme_dark_outlineVariant = Color(0xFF4E4639)
    val md_theme_dark_scrim = Color(0xFF000000)
}