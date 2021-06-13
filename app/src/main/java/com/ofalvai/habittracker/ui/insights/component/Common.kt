package com.ofalvai.habittracker.ui.insights.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.theme.AppIcons
import com.ofalvai.habittracker.ui.theme.AppTextStyle
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme

@Composable
fun InsightCard(
    iconPainter: Painter,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            InsightHeader(iconPainter, title, description)

            content()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InsightHeader(
    iconPainter: Painter,
    title: String,
    description: String
) {
    Column(Modifier.fillMaxWidth()) {
        var expanded by remember { mutableStateOf(false) }

        Row {
            Icon(
                painter = iconPainter,
                contentDescription = title,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = title,
                style = AppTextStyle.insightCardTitle,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 17.dp),
            )
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(
                    painter = AppIcons.InfoOutlined,
                    contentDescription = stringResource(R.string.insights_more_info),
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Text(
                text = description,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@Composable
fun EmptyView(label: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Icon(
            modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally).alpha(0.5f),
            painter = painterResource(R.drawable.ic_insights_placeholder),
            contentDescription = stringResource(R.string.common_empty)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewInsightCard() {
    HabitTrackerTheme {
        InsightCard(
            modifier = Modifier.padding(16.dp),
            iconPainter = AppIcons.Habits,
            title = "Test stats",
            description = "Heatmap shows you the number of habits done every day. Darker days mean more completed habits on a given day."
        ) {
            EmptyView(label = "Test label")
        }
    }
}