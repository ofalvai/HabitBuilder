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

package com.ofalvai.habittracker.feature.misc.settings

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ofalvai.habittracker.core.ui.component.AppDefaultAppBar
import com.ofalvai.habittracker.feature.misc.R
import kotlinx.collections.immutable.ImmutableList
import com.ofalvai.habittracker.core.ui.R as coreR

@Composable
fun LicensesScreen(viewModel: LicensesViewModel, navigateBack: () -> Unit) {
    val dependencies by viewModel.dependencies.collectAsState()

    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        AppDefaultAppBar(
            title = { Text(stringResource(R.string.licenses_title)) },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.Rounded.ArrowBack, stringResource(coreR.string.common_back))
                }
            }
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
                style = MaterialTheme.typography.bodyLarge,
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
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = dependency.artifact,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = dependency.license?.name ?: stringResource(R.string.licenses_unknown_license),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier.align(Alignment.CenterVertically).fillMaxWidth()
                    )
                }

                AnimatedVisibility(visible = isExpanded && dependency.url != null) {
                    Text(
                        text = dependency.url!!,
                        style = MaterialTheme.typography.bodySmall.copy(
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