package com.ofalvai.habittracker.ui

import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument

sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Dashboard : Screen("screen")

    object Insights : Screen("insights")

    object AddHabit : Screen("add_habit")

    object HabitDetails : Screen(
        route = "habit_details/{id}",
        arguments = listOf(navArgument("id") {
            type = NavType.IntType
        })
    ) {
        fun idFrom(arguments: Bundle?): Int {
            return arguments?.getInt("id")!!
        }

        fun buildRoute(habitId: Int) = "habit_details/$habitId"
    }

    object Settings : Screen("settings")

    object About : Screen("about")

    object Licenses : Screen("licenses")
}