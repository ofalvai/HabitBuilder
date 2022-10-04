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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.core.common.Telemetry
import com.ofalvai.habittracker.core.model.HabitId
import com.ofalvai.habittracker.core.ui.theme.AppTheme
import com.ofalvai.habittracker.feature.dashboard.ui.addhabit.AddHabitScreen
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.DashboardScreen
import com.ofalvai.habittracker.feature.dashboard.ui.habitdetail.HabitDetailScreen
import com.ofalvai.habittracker.feature.insights.ui.InsightsScreen
import com.ofalvai.habittracker.feature.misc.archive.ArchiveScreen
import com.ofalvai.habittracker.feature.misc.export.ExportScreen
import com.ofalvai.habittracker.feature.misc.settings.LicensesScreen
import com.ofalvai.habittracker.feature.misc.settings.SettingsScreen
import com.ofalvai.habittracker.ui.settings.DebugSettings

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppTheme {
                val navController = rememberAnimatedNavController()
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = MaterialTheme.colors.isLight
                val scaffoldState = rememberScaffoldState()

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }

                LaunchedEffect(navController) {
                    navController.addOnDestinationChangedListener(onDestinationChanged)
                }

                Scaffold(
                    bottomBar = { AppBottomNavigation(navController) },
                    scaffoldState = scaffoldState,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Screens(navController, scaffoldState, innerPadding)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Screens(
    navController: NavHostController,
    scaffoldState: ScaffoldState,
    padding: PaddingValues
) {
    val vmFactory = Dependencies.viewModelFactory
    val navigateBack: () -> Unit = { navController.popBackStack() }
    val navigateToSettings: () -> Unit = { navController.navigate(Destination.Settings) }
    val navigateToArchive: () -> Unit = { navController.navigate(Destination.Archive) }
    val navigateToExport: () -> Unit = { navController.navigate(Destination.Export) }
    val navigateToHabitDetails: (HabitId) -> Unit = { habitId ->
        val route = Destination.HabitDetails.buildRoute(habitId = habitId)
        navController.navigate(route)
    }

    AnimatedNavHost(
        navController,
        startDestination = Destination.Dashboard.route,
        modifier = Modifier.padding(padding).fillMaxSize()
    ) {
        appDestination(Destination.Dashboard) {
            DashboardScreen(
                vmFactory,
                scaffoldState,
                navigateToHabitDetails,
                navigateToAddHabit = { navController.navigate(Destination.AddHabit.route) },
                navigateToSettings,
                navigateToArchive,
                navigateToExport
            )
        }
        appDestination(Destination.Insights) {
            InsightsScreen(
                vmFactory,
                navigateToArchive = navigateToArchive,
                navigateToSettings = navigateToSettings,
                navigateToExport = navigateToExport,
                navigateToHabitDetails = navigateToHabitDetails
            )
        }
        appDestination(Destination.AddHabit) { AddHabitScreen(vmFactory, navigateBack) }
        appDestination(Destination.HabitDetails) { backStackEntry ->
            HabitDetailScreen(
                vmFactory,
                habitId = Destination.HabitDetails.idFrom(backStackEntry.arguments),
                navigateBack
            )
        }
        appDestination(Destination.Settings) {
            SettingsScreen(
                vmFactory,
                navigateBack,
                navigateToLicenses = { navController.navigate(Destination.Licenses.route) },
                debugSettings = { DebugSettings() }
            )
        }
        appDestination(Destination.Licenses) {
            LicensesScreen(vmFactory, navigateBack = { navController.popBackStack() })
        }
        appDestination(Destination.Archive) {
            ArchiveScreen(
                vmFactory,
                scaffoldState,
                navigateBack
            )
        }
        appDestination(Destination.Export) { ExportScreen(vmFactory, navigateBack) }
    }
}

private val onDestinationChanged: (NavController, NavDestination, Bundle?) -> Unit =
    { _, destination, arguments ->
        Dependencies.telemetry.leaveBreadcrumb(
            message = destination.route ?: "no-route",
            metadata = arguments?.let { mapOf("arguments" to it.toString()) } ?: emptyMap(),
            type = Telemetry.BreadcrumbType.Navigation
        )
    }