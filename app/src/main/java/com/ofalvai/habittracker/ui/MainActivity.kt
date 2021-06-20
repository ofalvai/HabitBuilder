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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.dashboard.AddHabitScreen
import com.ofalvai.habittracker.ui.dashboard.Dashboard
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailScreen
import com.ofalvai.habittracker.ui.insights.InsightsScreen
import com.ofalvai.habittracker.ui.settings.AboutScreen
import com.ofalvai.habittracker.ui.settings.LicensesScreen
import com.ofalvai.habittracker.ui.settings.SettingsScreen
import com.ofalvai.habittracker.ui.theme.AppIcons
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            HabitTrackerTheme {
                ProvideWindowInsets {
                    val navController = rememberNavController()
                    val systemUiController = rememberSystemUiController()
                    val useDarkIcons = MaterialTheme.colors.isLight
                    val scaffoldState = rememberScaffoldState()

                    SideEffect {
                        systemUiController.setSystemBarsColor(
                            color = Color.Transparent,
                            darkIcons = useDarkIcons
                        )
                    }

                    Scaffold(
                        bottomBar = { AppBottomNavigation(navController) },
                        scaffoldState = scaffoldState
                    ) { innerPadding ->
                        Screens(navController, scaffoldState, innerPadding)
                    }
                }
            }
        }
    }
}

@Composable
private fun Screens(navController: NavHostController,
                    scaffoldState: ScaffoldState,
                    padding: PaddingValues
) {
    NavHost(
        navController,
        startDestination = Screen.Dashboard.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(Screen.Dashboard.route) { Dashboard(navController, scaffoldState) }
        composable(Screen.Insights.route) { InsightsScreen(navController) }
        composable(Screen.AddHabit.route) { AddHabitScreen(navController) }
        composable(
            Screen.HabitDetails.route,
            arguments = Screen.HabitDetails.arguments
        ) { backStackEntry ->
            HabitDetailScreen(
                habitId = Screen.HabitDetails.idFrom(backStackEntry.arguments),
                navController = navController
            )
        }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.About.route) { AboutScreen(navController) }
        composable(Screen.Licenses.route) { LicensesScreen(navController) }
    }
}

@Composable
fun ContentWithPlaceholder(
    showPlaceholder: Boolean,
    placeholder: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    if (showPlaceholder) {
        placeholder()
    } else {
        content()
    }
}

@Composable
fun TextFieldError(
    modifier: Modifier = Modifier,
    textError: String
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = textError,
            modifier = Modifier.fillMaxWidth(),
            style = LocalTextStyle.current.copy(color = MaterialTheme.colors.error)
        )
    }
}

@Composable
private fun AppBottomNavigation(navController: NavController) {
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
                rootScreen = Screen.Dashboard,
                icon = { Icon(AppIcons.Habits, stringResource(R.string.tab_dashboard)) },
                label = stringResource(R.string.tab_dashboard),
                currentDestination = currentDestination,
                navController = navController
            )
            AppBottomNavigationItem(
                rootScreen = Screen.Insights,
                icon = { Icon(AppIcons.Insights, stringResource(R.string.tab_insights)) },
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
    icon: @Composable () -> Unit,
    label: String,
    currentDestination: NavDestination?,
    navController: NavController
) {
    BottomNavigationItem(
        icon = icon,
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