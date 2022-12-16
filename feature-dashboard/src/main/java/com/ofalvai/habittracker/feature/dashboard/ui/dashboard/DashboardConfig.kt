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

package com.ofalvai.habittracker.feature.dashboard.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.ui.component.HorizontalGrid
import com.ofalvai.habittracker.core.ui.theme.LocalAppColors
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.feature.dashboard.R
import com.ofalvai.habittracker.feature.dashboard.ui.model.DashboardConfig
import com.ofalvai.habittracker.core.ui.R as coreR


@Composable
fun DashboardConfigDialog(
    isVisible: Boolean,
    config: DashboardConfig,
    onConfigSelected: (DashboardConfig) -> Unit,
    onDismissed: () -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismissed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                DialogContent(config, onConfigSelected, onDismissed)
            }
        }
    }
}

@Composable
private fun DialogContent(
    config: DashboardConfig,
    onConfigSelected: (DashboardConfig) -> Unit,
    onDismissed: () -> Unit
) {
    var selectedConfig by remember { mutableStateOf(config) }

    Column(Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.dashboard_config_dialog_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(32.dp))
        ConfigOption(
            isSelected = selectedConfig == DashboardConfig.FiveDay,
            onSelected = {
                selectedConfig = DashboardConfig.FiveDay
                onConfigSelected(selectedConfig)
            }
        ) {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_config_dialog_fiveday),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                FiveDayOutline()
            }
        }
        Spacer(Modifier.height(16.dp))
        ConfigOption(
            isSelected = selectedConfig == DashboardConfig.MiniCalendar,
            onSelected = {
                selectedConfig = DashboardConfig.MiniCalendar
                onConfigSelected(selectedConfig)
            }
        ) {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_config_dialog_minicalendar),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                MiniCalendarOutline()
            }
        }
        Spacer(Modifier.height(16.dp))
        ConfigOption(
            isSelected = selectedConfig == DashboardConfig.Compact,
            onSelected = {
                selectedConfig = DashboardConfig.Compact
                onConfigSelected(selectedConfig)
            }
        ) {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_config_dialog_compact),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                CompactOutline()
            }
        }
        Button(
            onClick = onDismissed,
            modifier = Modifier.align(Alignment.End).padding(top = 16.dp)
        ) {
            Text(text = stringResource(coreR.string.common_save))
        }
    }
}

@Composable
private fun ConfigOption(
    isSelected: Boolean,
    onSelected: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier
        .clickable { onSelected() }
        .clip(MaterialTheme.shapes.medium)
        .then(
            if (isSelected) Modifier.border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            ) else Modifier
        )
        .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun FiveDayOutline() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.medium,
                color = LocalAppColors.current.gray2
            )
            .padding(12.dp)
    ) {
        Box(Modifier.fillMaxWidth(fraction = 0.4f).height(12.dp).placeholderBackground())
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth(fraction = 0.25f).height(4.dp).placeholderBackground())
        Spacer(Modifier.height(8.dp))
        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FiveDayOutlineCircle(toggled = true)
            FiveDayOutlineCircle(toggled = false)
            FiveDayOutlineCircle(toggled = false)
            FiveDayOutlineCircle(toggled = true)
            FiveDayOutlineCircle(toggled = true)
        }
    }
}

@Composable
private fun FiveDayOutlineCircle(toggled: Boolean) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(24.dp)
            .then(
                if (toggled) Modifier.placeholderBackground() else Modifier.border(
                    1.dp,
                    LocalAppColors.current.gray2,
                    CircleShape
                )
            )
    )
}

@Composable
private fun MiniCalendarOutline() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.medium,
                color = LocalAppColors.current.gray2
            )
            .padding(12.dp)
    ) {
        Box(Modifier.fillMaxWidth(fraction = 0.4f).height(12.dp).placeholderBackground())
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(width = 56.dp, height = 20.dp)
                    .placeholderBackground(shape = RoundedCornerShape(percent = 50))
            )

            Column {
                Row(modifier = Modifier.padding(vertical = 1.dp)) {
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = false)
                }
                Row(modifier = Modifier.padding(vertical = 1.dp)) {
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = false)
                }
                Row(modifier = Modifier.padding(vertical = 1.dp)) {
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = true)
                    MiniCalendarOutlineDot(toggled = false)
                    MiniCalendarOutlineDot(toggled = true)
                }
            }
        }
    }
}

@Composable
private fun MiniCalendarOutlineDot(toggled: Boolean) {
    Box(
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .size(8.dp)
            .then(
                if (toggled) Modifier.placeholderBackground() else Modifier.background(
                    LocalAppColors.current.gray1,
                )
            )
    )
}

@Composable
private fun CompactOutline() {
    Column(
        Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth(fraction = 0.2f).height(4.dp).placeholderBackground())
        Spacer(Modifier.height(4.dp))
        HorizontalGrid(Modifier.fillMaxWidth()) {
            CompactOutlineBox(toggled = false)
            CompactOutlineBox(toggled = false)
            CompactOutlineBox(toggled = true)
            CompactOutlineBox(toggled = false)
            CompactOutlineBox(toggled = false)
            CompactOutlineBox(toggled = true)
            CompactOutlineBox(toggled = true)
        }
    }
}

private fun Modifier.placeholderBackground(shape: Shape = RectangleShape) = this.composed {
    this.background(LocalAppColors.current.gray2, shape)
}

@Composable
private fun CompactOutlineBox(toggled: Boolean) {
    Box(
        Modifier
            .aspectRatio(1f)
            .then(
                if (toggled) Modifier.placeholderBackground() else Modifier.background(
                    LocalAppColors.current.gray1
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.background)
    )
}


@Composable
@Preview
@ShowkaseComposable(name = "Config dialog", "Dashboard")
fun PreviewDialogContent() {
    PreviewTheme {
        var config by remember { mutableStateOf(DashboardConfig.FiveDay) }
        DialogContent(config, { config = it }, {})
    }
}
