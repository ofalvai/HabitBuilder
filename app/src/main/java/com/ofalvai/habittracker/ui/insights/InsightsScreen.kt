package com.ofalvai.habittracker.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.ui.insights.component.Heatmap
import com.ofalvai.habittracker.ui.insights.component.TopDays
import com.ofalvai.habittracker.ui.insights.component.TopHabits
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun InsightsScreen(navController: NavController) {
    val viewModel: InsightsViewModel = viewModel(factory = Dependencies.viewModelFactory)

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Heatmap(viewModel)

        TopHabits(viewModel)

        TopDays(viewModel)
    }
}