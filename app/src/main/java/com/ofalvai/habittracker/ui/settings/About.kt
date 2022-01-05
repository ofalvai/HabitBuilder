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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.BuildConfig
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.Destination
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme

private const val SOURCE_URL = "https://github.com/ofalvai/HabitTracker"
private const val MARKET_URL = "market://details?id=${BuildConfig.APPLICATION_ID}"

@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current

    AboutScreen(
        version = "${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE})",
        onRateClick = {
            val uri = MARKET_URL.toUri()
            val intent = Intent(Intent.ACTION_VIEW).apply { data = uri }
            context.startActivity(intent)
        },
        onGitHubClick = {
            val uri = SOURCE_URL.toUri()
            val intent = Intent(Intent.ACTION_VIEW).apply { data = uri }
            context.startActivity(intent)
        },
        onOpenSourceClick = { navController.navigate(Destination.Licenses.route) }
    )
}

@Composable
private fun AboutScreen(
    version: String,
    onOpenSourceClick: () -> Unit,
    onGitHubClick: () -> Unit,
    onRateClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .padding(32.dp)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.h4,
        )
        Text(
            text = stringResource(R.string.about_version_label, version),
            style = MaterialTheme.typography.body2
        )
        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = onRateClick) {
            Text(text = stringResource(R.string.about_button_rate))
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onGitHubClick) {
            Text(text = stringResource(R.string.about_button_source))
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onOpenSourceClick) {
            Text(text = stringResource(R.string.about_button_licenses))
        }
    }
}

@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
private fun PreviewAboutScreen() {
    HabitTrackerTheme {
        AboutScreen(
            version = "1.0.0 release",
            onRateClick = {},
            onGitHubClick = {},
            onOpenSourceClick = {}
        )
    }
}