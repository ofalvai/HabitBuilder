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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ofalvai.habittracker.core.common.AppPreferences
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences
    @Inject
    lateinit var navigationTelemetryLogger: NavigationTelemetryLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val isDynamicColor by appPreferences.dynamicColorEnabled.collectAsState()
            AppTheme(isDynamicColor = isDynamicColor) {
                val navController = rememberNavController()
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                val snackbarHostState = remember { SnackbarHostState() }

                DisposableEffect(systemUiController, useDarkIcons) {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                    onDispose { }
                }

                LaunchedEffect(navController) {
                    navController.addOnDestinationChangedListener(navigationTelemetryLogger)
                }

                Scaffold(
                    bottomBar = { AppNavigationBar(navController) },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize(),
                    // Don't consume any insets. Even though a TopAppBar is not defined here, each
                    // screen adds one and handles the status bar inset, while giving each screen
                    // the chance to draw behind the status bar. The navigation bar inset is handled
                    // by the bottomBar too.
                    contentWindowInsets = WindowInsets(top = 0.dp)
                ) { innerPadding ->
                    Screens(navController, snackbarHostState, innerPadding)
                }
            }
        }
    }
}

@Composable
private fun Screens(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues
) {
    val navigateBack: () -> Unit = { navController.popBackStack() }
    val navigateToSettings: () -> Unit = { navController.navigate(Destination.Settings) }
    val navigateToArchive: () -> Unit = { navController.navigate(Destination.Archive) }
    val navigateToExport: () -> Unit = { navController.navigate(Destination.Export) }
    val navigateToHabitDetails: (HabitId) -> Unit = { habitId ->
        val route = Destination.HabitDetails.buildRoute(habitId = habitId)
        navController.navigate(route)
    }

    NavHost(
        navController,
        startDestination = Destination.Dashboard.route,
        modifier = Modifier.padding(padding).fillMaxSize()
    ) {
        appDestination(Destination.Dashboard) {
            DashboardScreen(
                hiltViewModel(),
                snackbarHostState,
                navigateToHabitDetails,
                navigateToAddHabit = { navController.navigate(Destination.AddHabit.route) },
                navigateToSettings,
                navigateToArchive,
                navigateToExport
            )
        }
        appDestination(Destination.Insights) {
            InsightsScreen(
                hiltViewModel(),
                navigateToArchive = navigateToArchive,
                navigateToSettings = navigateToSettings,
                navigateToExport = navigateToExport,
                navigateToHabitDetails = navigateToHabitDetails
            )
        }
        appDestination(Destination.AddHabit) { AddHabitScreen(hiltViewModel(), navigateBack) }
        appDestination(Destination.HabitDetails) { backStackEntry ->
            HabitDetailScreen(
                hiltViewModel(),
                habitId = Destination.HabitDetails.idFrom(backStackEntry.arguments),
                navigateBack
            )
        }
        appDestination(Destination.Settings) {
            SettingsScreen(
                hiltViewModel(),
                navigateBack,
                navigateToLicenses = { navController.navigate(Destination.Licenses.route) },
                debugSettings = { DebugSettings() }
            )
        }
        appDestination(Destination.Licenses) {
            LicensesScreen(hiltViewModel(), navigateBack = { navController.popBackStack() })
        }
        appDestination(Destination.Archive) {
            ArchiveScreen(
                hiltViewModel(),
                snackbarHostState,
                navigateBack
            )
        }
        appDestination(Destination.Export) { ExportScreen(hiltViewModel(), navigateBack) }
    }
}

class NavigationTelemetryLogger @Inject constructor(
    private val telemetry: Telemetry
) : NavController.OnDestinationChangedListener {
    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        telemetry.leaveBreadcrumb(
            message = destination.route ?: "no-route",
            metadata = arguments?.let { mapOf("arguments" to it.toString()) } ?: emptyMap(),
            type = Telemetry.BreadcrumbType.Navigation
        )
    }
}
