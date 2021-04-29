package com.ofalvai.habittracker.ui.insights.component

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
fun TopDays(viewModel: InsightsViewModel) {
    val topDays by viewModel.habitTopDays.observeAsState(emptyList())

    InsightCard(
        iconPainter = AppIcons.Heatmap, // TODO
        title = stringResource(R.string.insights_topdays_title),
        description = "See how many habits you perform each day", // TODO
    ) {
        topDays.forEach {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = "${it.name}: ${it.top_day_of_week} (${it.action_count_on_day})"
            )
        }
    }
}