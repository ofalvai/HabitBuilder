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

package com.ofalvai.habittracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.core.ui.theme.CoreIcons

@Composable
fun AppBottomNavigation(navController: NavController) {
    // Recreating the BottomNavigation() composable because window insets and elevation don't play
    // nice together. We need to apply a background (behind the navbar), padding, and elevation
    // in the correct order. Otherwise the elevation bottom shadow will be rendered on top of the
    // background behind the navbar.
    Surface(
        color = MaterialTheme.colors.surface,
        elevation = 8.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            AppBottomNavigationItem(
                rootScreen = Destination.Dashboard,
                content = { Icon(CoreIcons.Habits, stringResource(R.string.tab_dashboard)) },
                label = stringResource(R.string.tab_dashboard),
                currentDestination = currentDestination,
                navController = navController
            )
            AppBottomNavigationItem(
                rootScreen = Destination.Insights,
                content = { Icon(AppIcons.Insights, stringResource(R.string.tab_insights)) },
                label = stringResource(R.string.tab_insights),
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
private fun RowScope.AppBottomNavigationItem(
    rootScreen: Screen,
    label: String,
    currentDestination: NavDestination?,
    navController: NavController,
    content: @Composable () -> Unit
) {
    BottomNavigationItem(
        icon = content,
        selected = currentDestination?.hierarchy?.any { it.route == rootScreen.route } == true,
        onClick = {
            navController.navigate(rootScreen.route) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id)
                // Avoid multiple copies of the same destination when
                // re-selecting the same item
                launchSingleTop = true
            }
        },
        label = { Text(label) }
    )
}