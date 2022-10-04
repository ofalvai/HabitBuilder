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

import androidx.compose.material.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.ofalvai.habittracker.core.ui.R

@OptIn(ExperimentalTextApi::class)
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@OptIn(ExperimentalTextApi::class)
private val lato = FontFamily(
    Font(googleFont = GoogleFont("Lato"), fontProvider = provider),
    Font(googleFont = GoogleFont("Lato"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Lato"), fontProvider = provider, weight = FontWeight.Bold)
)

@OptIn(ExperimentalTextApi::class)
private val patua = FontFamily(
    Font(googleFont = GoogleFont("Patua One"), fontProvider = provider)
)

val typography = Typography(
        defaultFontFamily = lato,
        button = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = 0.25.sp
        )
    )

object AppTextStyle {

    val screenTitle = typography.h6.copy(fontFamily = patua)

    val habitTitle = typography.h4.copy(fontFamily = patua, fontSize = 36.sp)

    val habitCompactTitle = typography.subtitle2.copy(fontFamily = patua)

    val habitSubtitle = typography.h6.copy(fontFamily = patua)

    val insightCardTitle = typography.h6.copy(fontWeight = FontWeight.Bold) // Lato doesn't have medium

    val singleStatValue = typography.h5.copy(fontFamily = patua)

}