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

package com.ofalvai.habittracker.ui.archive

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.common.ErrorView
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions

@Composable
fun ArchiveScreen(navController: NavController) {
    val viewModel = viewModel<ArchiveViewModel>(factory = Dependencies.viewModelFactory)

    val habits by viewModel.archivedHabitList.collectAsState(initial = Result.Loading)
    val onHabitUnarchive: (Habit) -> Unit = {
        viewModel.unarchiveHabit(it)
    }

    Column {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = { Text(stringResource(R.string.archive_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
                }
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )

        when (habits) {
            is Result.Success -> {
                ArchivedHabitList(
                    (habits as Result.Success<List<HabitWithActions>>).value, onHabitUnarchive
                )
            }
            Result.Loading -> {}
            is Result.Failure -> ErrorView(
                label = stringResource(R.string.dashboard_error),
                modifier = Modifier.statusBarsPadding()
            )
        }

    }
}

@Composable
fun ArchivedHabitList(
    habits: List<HabitWithActions>,
    onHabitUnarchive: (Habit) -> Unit
) {
    LazyColumn {
        items(habits) {
            Text(text = it.habit.name)
        }
    }
}