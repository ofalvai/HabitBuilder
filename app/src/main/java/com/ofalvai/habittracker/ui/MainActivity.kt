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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.dashboard.AddHabitScreen
import com.ofalvai.habittracker.ui.dashboard.Dashboard
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailScreen
import com.ofalvai.habittracker.ui.insights.InsightsScreen
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
                        bottomBar = {
                            AppBottomNavigation(
                                onDashboardSelected = { navController.navigate(Screen.Dashboard.route) },
                                onInsightsSelected = { navController.navigate(Screen.Insights.route) }
                            )
                        },
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
private fun AppBottomNavigation(
    onDashboardSelected: () -> Unit,
    onInsightsSelected: () -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }

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
            BottomNavigationItem(
                icon = { Icon(AppIcons.Habits, stringResource(R.string.tab_dashboard)) },
                selected = selectedIndex == 0,
                onClick = {
                    if (selectedIndex != 0) {
                        selectedIndex = 0
                        onDashboardSelected()
                    }
                },
                label = { Text(stringResource(R.string.tab_dashboard)) }
            )
            BottomNavigationItem(
                icon = { Icon(AppIcons.Insights, stringResource(R.string.tab_insights)) },
                selected = selectedIndex == 1,
                onClick = {
                    if (selectedIndex != 1) {
                        selectedIndex = 1
                        onInsightsSelected()
                    }
                },
                label = { Text(stringResource(R.string.tab_insights)) }
            )
        }
    }
}