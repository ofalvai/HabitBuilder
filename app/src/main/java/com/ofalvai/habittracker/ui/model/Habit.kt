package com.ofalvai.habittracker.ui.model

data class Habit(
    val id: Int = 0,
    val name: String,
    val color: Color
) {

    companion object {
        val DEFAULT_COLOR = Color.Yellow
    }

    enum class Color {
        // Note: enum order determines order on UI!

        Yellow,
        Green,
        Blue,
        Red
    }
}