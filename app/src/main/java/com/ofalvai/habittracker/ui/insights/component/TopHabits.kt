package com.ofalvai.habittracker.ui.insights.component

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.Screen
import com.ofalvai.habittracker.ui.insights.InsightsViewModel
import com.ofalvai.habittracker.ui.model.HabitId
import com.ofalvai.habittracker.ui.model.TopHabitItem
import com.ofalvai.habittracker.ui.theme.AppIcons
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import com.ofalvai.habittracker.ui.theme.habitInactive

@Composable
fun TopHabits(viewModel: InsightsViewModel, navController: NavController) {
    val topHabits by viewModel.topHabits.observeAsState(emptyList())

    val onHabitClick: (HabitId) -> Unit = {
        val route = Screen.HabitDetails.buildRoute(habitId = it)
        navController.navigate(route)
    }

    InsightCard(
        iconPainter = AppIcons.Habits,
        title = stringResource(R.string.insights_tophabits_title),
        description = "See your most often performed habits", // TODO: bar explanation
    ) {
        Column {
            TopHabitsTable(
                habits = topHabits,
                onHabitClick = onHabitClick
            )

//            TextButton(
//                onClick = onSeeAllClick,
//                modifier = Modifier.align(Alignment.End)
//            ) {
//                Text(text = stringResource(R.string.insights_tophabits_see_all))
//            }
        }
    }
}

@Composable
fun TopHabitsTable(
    habits: List<TopHabitItem>,
    onHabitClick: (HabitId) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        habits.forEachIndexed { index, element ->
            TopHabitsRow(
                index = index + 1,
                item = element,
                onClick = onHabitClick
            )
        }
    }
}

@Composable
fun TopHabitsRow(
    index: Int,
    item: TopHabitItem,
    onClick: (HabitId) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.width(24.dp)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = item.name,
            modifier = Modifier.weight(0.50f),
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            style = MaterialTheme.typography.body1,
        )

        Text(
            text = item.count.toString(),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(0.2f)
        )

        Spacer(Modifier.width(16.dp))

        HabitBar(
            progress = item.progress,
            modifier = Modifier.weight(0.2f)
        )

        IconButton(onClick = { onClick(item.habitId) }) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(
                    R.string.insights_tophabits_navigate,
                    item.name
                )
            )
        }
    }
}

@Composable
fun HabitBar(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    modifier: Modifier = Modifier,
) {
    val height = 8.dp
    val shape = RoundedCornerShape(4.dp)

    Box(modifier = modifier
            .clip(shape)
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colors.habitInactive)
    ) {
        Box(modifier = Modifier
            .clip(shape)
            .fillMaxWidth(fraction = progress)
            .height(height)
            .background(MaterialTheme.colors.primary)
        )
    }
}

@Preview(showBackground = true, widthDp = 300, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewTopHabitsTable() {
    val topHabits = listOf(
        TopHabitItem(
            habitId = 1,
            name = "Short name",
            count = 1567,
            progress = 1f
        ),
        TopHabitItem(
            habitId = 1,
            name = "Name",
            count = 153,
            progress = 0.8f
        ),
        TopHabitItem(
            habitId = 1,
            name = "Loooong name lorem ipsum dolor sit amet",
            count = 10,
            progress = 0.5f
        ),
        TopHabitItem(
            habitId = 1,
            name = "Meditation",
            count = 9,
            progress = 0.1f
        ),
        TopHabitItem(
            habitId = 1,
            name = "Workout",
            count = 3,
            progress = 0f
        )
    )

    HabitTrackerTheme {
        TopHabitsTable(
            habits = topHabits,
            onHabitClick = {  }
        )
    }
}