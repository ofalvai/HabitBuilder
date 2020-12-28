package com.ofalvai.habittracker.ui.model

import com.ofalvai.habittracker.ui.*
import androidx.compose.ui.graphics.Color as ComposeColor


data class Habit(
    val id: Int = 0,
    val name: String,
    val color: Color
) {
    enum class Color(val composeColor: ComposeColor) {
        Red(habitRed),
        Green(habitGreen),
        Blue(habitBlue),
        Yellow(habitYellow),
        Gray(habitGray),
        White(habitWhite)
    }
}