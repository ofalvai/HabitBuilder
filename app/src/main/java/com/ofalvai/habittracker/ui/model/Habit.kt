package com.ofalvai.habittracker.ui.model

import com.ofalvai.habittracker.ui.*
import androidx.compose.ui.graphics.Color as ComposeColor


data class Habit(
    val id: Int = 0,
    val name: String,
    val color: Color
) {

    companion object {
        val DEFAULT_COLOR = Color.Green
    }

    enum class Color(val composeColor: ComposeColor) {
        // Note: enum order determines order on UI!

        Green(habitGreen),
        Blue(habitBlue),
        Yellow(habitYellow),
        Red(habitRed)
    }
}