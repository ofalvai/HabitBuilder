package com.ofalvai.habittracker.ui

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
        val background = Color(0xFFFFF8EA)
        val surface = Color.White
        val onSurface = Color(0xFF262626)
        val onBackground = Color(0xFF262626)

        val habitRed = Color(0xFFE08F8F)
        val habitGreen = Color(0xFFAEC2B8)
        val habitBlue = Color(0xFF9EB2D1)
        val habitYellow = Color(0xFFFFCC79)
        val habitPink = Color(0xFFE08FB8)
    }

    object Dark {
        // TODO: check colors on dark background
        val habitRed = Color(0xFFE08F8F)
        val habitGreen = Color(0xFFAEC2B8)
        val habitBlue = Color(0xFF9EB2D1)
        val habitYellow = Color(0xFFFFCC79)
        val habitPink = Color(0xFFE08FB8)
    }
}

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