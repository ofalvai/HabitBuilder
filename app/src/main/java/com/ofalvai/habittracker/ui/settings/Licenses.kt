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

package com.ofalvai.habittracker.ui.settings

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import kotlinx.collections.immutable.ImmutableList
import com.ofalvai.habittracker.core.ui.R as coreR

@Composable
fun LicensesScreen(navController: NavController) {
    val viewModel: LicensesViewModel = viewModel(factory = Dependencies.viewModelFactory)
    val dependencies by viewModel.dependencies.collectAsState()

    Column {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = { Text(stringResource(R.string.licenses_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.ArrowBack, stringResource(coreR.string.common_back))
                }
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )

        DependencyList(dependencies)
    }
}

@Composable
private fun DependencyList(dependencies: ImmutableList<Dependency>) {
    val context = LocalContext.current
    val onUrlClick: (String) -> Unit = {
        val uri = it.toUri()
        val intent = Intent(Intent.ACTION_VIEW).apply { data = uri }
        context.startActivity(intent)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.licenses_description),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            )
        }

        items(dependencies) { dependency ->
            var isExpanded by remember { mutableStateOf(false) }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                Row {
                    Column {
                        Text(
                            text = dependency.group,
                            style = MaterialTheme.typography.caption.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = dependency.artifact,
                            style = MaterialTheme.typography.body2.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = dependency.license?.name ?: stringResource(R.string.licenses_unknown_license),
                        style = MaterialTheme.typography.caption,
                        textAlign = TextAlign.End,
                        modifier = Modifier.align(Alignment.CenterVertically).fillMaxWidth()
                    )
                }

                AnimatedVisibility(visible = isExpanded && dependency.url != null) {
                    Text(
                        text = dependency.url!!,
                        style = MaterialTheme.typography.caption.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { onUrlClick(dependency.url) }
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}