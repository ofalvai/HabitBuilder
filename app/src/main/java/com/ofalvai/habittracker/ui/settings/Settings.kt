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

package com.ofalvai.habittracker.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.BuildConfig
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.Destination
import com.ofalvai.habittracker.ui.theme.PreviewTheme
import com.ofalvai.habittracker.ui.theme.gray1

private const val SOURCE_URL = "https://github.com/ofalvai/HabitTracker"
private const val MARKET_URL = "market://details?id=${BuildConfig.APPLICATION_ID}"


@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel = viewModel<SettingsViewModel>(factory = Dependencies.viewModelFactory)
    val context = LocalContext.current

    val onBack: () -> Unit = { navController.popBackStack() }
    val onRateClick = {
        val uri = MARKET_URL.toUri()
        val intent = Intent(Intent.ACTION_VIEW).apply { data = uri }
        context.startActivity(intent)
    }
    val onSourceClick = {
        val uri = SOURCE_URL.toUri()
        val intent = Intent(Intent.ACTION_VIEW).apply { data = uri }
        context.startActivity(intent)
    }
    val onLicensesClick = { navController.navigate(Destination.Licenses.route) }

    val crashReportingEnabled by viewModel.crashReportingEnabled.collectAsState()
    val onCrashReportingChange: (Boolean) -> Unit = {
        viewModel.setCrashReportingEnabled(it)
        Toast.makeText(context, R.string.settings_crash_reporting_restart_message, Toast.LENGTH_LONG).show()
    }

    SettingsScreen(
        crashReportingEnabled,
        onBack,
        onRateClick,
        onSourceClick,
        onLicensesClick,
        onCrashReportingChange,
    )
}

@Composable
fun SettingsScreen(
    crashReportingEnabled: Boolean,
    onBack: () -> Unit,
    onRateClick: () -> Unit,
    onSourceClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onCrashReportingChange: (Boolean) -> Unit
) {
    Column(
        Modifier
            .background(MaterialTheme.colors.background)
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
                }
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
        ) {
            SwitchSetting(
                name = stringResource(R.string.settings_item_crash_reporting),
                checked = crashReportingEnabled,
                onCheckedChange = onCrashReportingChange
            )

            SettingHeader(name = stringResource(R.string.settings_header_about))
            TextRow(
                name = stringResource(R.string.app_name),
                subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE})"
            )
            NavigationSetting(
                name = stringResource(R.string.settings_item_rate),
                onClick = onRateClick
            )
            NavigationSetting(
                name = stringResource(R.string.settings_item_view_source),
                onClick = onSourceClick
            )
            NavigationSetting(
                name = stringResource(R.string.settings_item_licenses),
                onClick = onLicensesClick
            )

            DebugSettings() // no-op in release variant
        }
    }
}

@Composable
internal fun SettingHeader(name: String) {
    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colors.gray1))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 72.dp, end = 16.dp, bottom = 2.dp, top = 16.dp),
            text = name,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.secondaryVariant
        )
    }

}

@Composable
private fun SwitchSetting(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp, start = 56.dp)
        )
        Spacer(Modifier.weight(1f))
        Switch(checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun NavigationSetting(
    name: String,
    onClick: () -> Unit
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 72.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        text = name,
        style = MaterialTheme.typography.body1
    )
}

@Composable
internal fun TextRow(name: String, subtitle: String) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(start = 56.dp),
            text = name,
            style = MaterialTheme.typography.body1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.fillMaxWidth().padding(start = 56.dp)
        )
    }
}

@Preview
@ShowkaseComposable(name = "Screen", group = "Settings")
@Composable
fun PreviewSettingsScreen() {
    PreviewTheme {
        SettingsScreen(
            crashReportingEnabled = true,
            onBack = {},
            onSourceClick = {},
            onLicensesClick = {},
            onRateClick = {},
            onCrashReportingChange = {}
        )
    }
}