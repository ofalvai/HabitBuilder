/*
 * Copyright 2021 Olivér Falvai
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

package com.ofalvai.habittracker.ui

import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument

sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Dashboard : Screen("dashboard")

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