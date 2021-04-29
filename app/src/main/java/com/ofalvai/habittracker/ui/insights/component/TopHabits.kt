package com.ofalvai.habittracker.ui.insights.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.insights.InsightsViewModel
import com.ofalvai.habittracker.ui.theme.AppIcons

@Composable
fun TopHabits(viewModel: InsightsViewModel) {
    val topHabits by viewModel.mostSuccessfulHabits.observeAsState(emptyList())

    InsightCard(
        iconPainter = AppIcons.Heatmap, // TODO
        title = stringResource(R.string.insights_tophabits_title),
        description = "See your most often performed habits", // TODO: bar explanation
    ) {
        Column {
            topHabits.forEach {
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "${it.name}: ${it.count}"
                )
            }
        }
    }
}