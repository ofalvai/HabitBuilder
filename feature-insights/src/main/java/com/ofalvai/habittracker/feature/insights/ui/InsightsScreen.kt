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

package com.ofalvai.habittracker.feature.insights.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.core.model.HabitId
import com.ofalvai.habittracker.core.ui.component.AppBarOverflowMenuAction
import com.ofalvai.habittracker.core.ui.component.AppDefaultRootAppBar
import com.ofalvai.habittracker.core.ui.theme.AppTextStyle
import com.ofalvai.habittracker.core.ui.theme.CoreIcons
import com.ofalvai.habittracker.feature.insights.ui.component.Heatmap
import com.ofalvai.habittracker.feature.insights.ui.component.TopDays
import com.ofalvai.habittracker.feature.insights.ui.component.TopHabits
import com.ofalvai.habittracker.core.ui.R as coreR
import com.ofalvai.habittracker.feature.insights.R as insightsR

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    navigateToSettings: () -> Unit,
    navigateToArchive: () -> Unit,
    navigateToExport: () -> Unit,
    navigateToHabitDetails: (HabitId) -> Unit
) {
    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        InsightsAppBar(
            onSettingsClick = navigateToSettings,
            onArchiveClick = navigateToArchive,
            onExportClick = navigateToExport
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Heatmap(viewModel)

            TopHabits(viewModel, navigateToHabitDetails)

            TopDays(viewModel, navigateToHabitDetails)
        }
    }
}

@Composable
private fun InsightsAppBar(
    onSettingsClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onExportClick: () -> Unit
) {
    AppDefaultRootAppBar(
        title = {
            Text(
                text = stringResource(insightsR.string.insights_screen_title),
                style = AppTextStyle.screenTitle
            )
        },
        actions = {
            AppBarOverflowMenuAction {
                DropdownMenuItem(
                    onClick = onArchiveClick,
                    text = { Text(stringResource(coreR.string.menu_archive)) },
                    leadingIcon = { Icon(painter = CoreIcons.Archive, contentDescription = null) }
                )
                DropdownMenuItem(
                    onClick = onExportClick,
                    text = { Text(stringResource(coreR.string.menu_export)) },
                    leadingIcon = { Icon(painter = CoreIcons.Export, contentDescription = null) }
                )
                DropdownMenuItem(
                    onClick = onSettingsClick,
                    text = { Text(stringResource(coreR.string.menu_settings)) },
                    leadingIcon = { Icon(painter = CoreIcons.Settings, contentDescription = null) }
                )
            }
        },
    )
}