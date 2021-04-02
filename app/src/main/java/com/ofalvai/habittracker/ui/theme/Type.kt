package com.ofalvai.habittracker.ui.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ofalvai.habittracker.R

private val lato = FontFamily(
    Font(R.font.lato_regular),
    Font(R.font.lato_bold, weight = FontWeight.Bold)
)

private val patua = FontFamily(
    Font(R.font.patua_one_regular)
)

val typography: Typography
    @Composable
    get() = Typography(
        defaultFontFamily = lato,
    )

object AppTextStyle {

    val habitTitle: TextStyle
        @Composable
        get() = typography.h4.copy(fontFamily = patua, fontSize = 36.sp)

    val habitCompactTitle: TextStyle
        @Composable
        get() = typography.subtitle2.copy(fontFamily = patua)

    val habitSubtitle: TextStyle
        @Composable
        get() = typography.h6.copy(fontFamily = patua)

}