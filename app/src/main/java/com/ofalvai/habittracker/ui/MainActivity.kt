package com.ofalvai.habittracker.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.dashboard.AddHabitScreen
import com.ofalvai.habittracker.ui.dashboard.Dashboard
import com.ofalvai.habittracker.ui.habitdetail.HabitDetailScreen
import dev.chrisbanes.accompanist.insets.*

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<HabitViewModel> { Dependencies.viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            HabitTrackerTheme {
                ProvideWindowInsets {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = {
                            AppBottomNavigation(
                                onDashboardSelected = { navController.navigate(Screen.Dashboard.route) },
                                onInsightsSelected = { navController.navigate(Screen.Insights.route) }
                            )
                        }
                    ) {
                        NavHost(navController, startDestination = Screen.Dashboard.route) {
                            composable(Screen.Dashboard.route) { Dashboard(viewModel, navController) }
                            composable(Screen.Insights.route) { Text(modifier = Modifier.statusBarsPadding(), text = "Work In Progress") }
                            composable(Screen.AddHabit.route) {
                                AddHabitScreen(
                                    viewModel,
                                    navController
                                )
                            }
                            composable(
                                Screen.HabitDetails.route,
                                arguments = Screen.HabitDetails.arguments
                            ) { backStackEntry ->
                                HabitDetailScreen(
                                    habitId = Screen.HabitDetails.idFrom(backStackEntry.arguments),
                                    viewModel = viewModel,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
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
        Spacer(modifier = Modifier.preferredWidth(16.dp))
        Text(
            text = textError,
            modifier = Modifier.fillMaxWidth(),
            style = AmbientTextStyle.current.copy(color = MaterialTheme.colors.error)
        )
    }
}

@Composable
fun AppBottomNavigation(
    onDashboardSelected: () -> Unit,
    onInsightsSelected: () -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }

    // Recreating the BottomNavigation() composable because window insets and elevation don't play
    // nice together. We need to apply a background (behind the navbar), padding, and elevation
    // in the correct order. Otherwise the elevation bottom shadow will be rendered on top of the
    // background behind the navbar.
    Surface(
        color = Color.White, // TODO theme
        elevation = 8.dp,
    ) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().preferredHeight(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.CheckCircle, stringResource(R.string.tab_dashboard)) },
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
                icon = { Icon(Icons.Filled.DateRange, stringResource(R.string.tab_dashboard)) },
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