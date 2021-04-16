package com.ofalvai.habittracker.ui.insights

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ofalvai.habittracker.Dependencies
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun InsightsScreen(navController: NavController) {
    val viewModel: InsightsViewModel = viewModel(factory = Dependencies.viewModelFactory)

    val mostSuccessfulHabits by viewModel.mostSuccessfulHabits.observeAsState(emptyList())
    val habitTopDays by viewModel.habitTopDays.observeAsState(emptyList())

    Column(Modifier.statusBarsPadding()) {
        Text(text = "Most successful habits:")

        mostSuccessfulHabits.forEach {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = "${it.name}: ${it.count}"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Top days:")
        habitTopDays.forEach {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = "${it.name}: ${it.top_day_of_week} (${it.action_count_on_day})"
            )
        }
    }
}