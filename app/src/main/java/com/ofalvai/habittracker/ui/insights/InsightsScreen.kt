/*
 * Copyright 2021 Olivér Falvai
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

package com.ofalvai.habittracker.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.Destination
import com.ofalvai.habittracker.ui.common.AppBar
import com.ofalvai.habittracker.ui.insights.component.Heatmap
import com.ofalvai.habittracker.ui.insights.component.TopDays
import com.ofalvai.habittracker.ui.insights.component.TopHabits
import com.ofalvai.habittracker.ui.theme.AppTextStyle

@Composable
fun InsightsScreen(navController: NavController) {
    val viewModel: InsightsViewModel = viewModel(factory = Dependencies.viewModelFactory)

    Column(Modifier.statusBarsPadding()) {
        InsightsAppBar(onAboutClick = { navController.navigate(Destination.About.route) })

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Heatmap(viewModel)

            TopHabits(viewModel, navController)

            TopDays(viewModel, navController)
        }
    }
}

@Composable
private fun InsightsAppBar(onAboutClick: () -> Unit) {
    AppBar(
        title = {
            Text(
                text = stringResource(R.string.insights_screen_title),
                style = AppTextStyle.screenTitle
            )
        },
        dropdownMenuItems = {
            DropdownMenuItem(onClick = onAboutClick) {
                Text(stringResource(R.string.menu_about))
            }
        }
    )
}