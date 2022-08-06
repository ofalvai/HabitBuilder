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

package com.ofalvai.habittracker.ui.insights.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.core.ui.theme.AppTextStyle
import com.ofalvai.habittracker.core.ui.theme.CoreIcons
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.core.ui.theme.surfaceVariant
import com.ofalvai.habittracker.ui.AppIcons
import com.ofalvai.habittracker.core.ui.R as coreR

@Composable
fun InsightCard(
    iconPainter: Painter,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.background(MaterialTheme.colors.surfaceVariant, shape = MaterialTheme.shapes.medium)) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)) {
            InsightHeader(iconPainter, title, description)

            content()
        }
    }
}

@Composable
fun InsightHeader(
    iconPainter: Painter,
    title: String,
    description: String
) {
    Column(Modifier.fillMaxWidth()) {
        var expanded by remember { mutableStateOf(false) }

        Row(Modifier.fillMaxWidth()) {
            Icon(
                painter = iconPainter,
                contentDescription = title,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = title,
                style = AppTextStyle.insightCardTitle,
                modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.CenterVertically),
            )
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.layout { measurable, constraints ->
                    // Align to end of parent
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(constraints.maxWidth - (placeable.width), 0)
                    }
                },
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
            contentDescription = stringResource(coreR.string.common_empty)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
        )
    }
}

@Preview
@ShowkaseComposable(name = "Card", group = "Insights")
@Composable
fun PreviewInsightCard() {
    PreviewTheme {
        InsightCard(
            modifier = Modifier.padding(16.dp),
            iconPainter = CoreIcons.Habits,
            title = "Test stats",
            description = "Heatmap shows you the number of habits done every day. Darker days mean more completed habits on a given day."
        ) {
            EmptyView(label = "Test label")
        }
    }
}