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

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.android.showkase.annotation.ShowkaseRoot
import com.airbnb.android.showkase.annotation.ShowkaseRootModule
import com.airbnb.android.showkase.models.Showkase
import com.ofalvai.habittracker.feature.misc.settings.NavigationSetting
import com.ofalvai.habittracker.feature.misc.settings.SettingHeader

@ShowkaseRoot
class AppRootModule : ShowkaseRootModule

@Composable
fun DebugSettings() {
    val viewModel: DebugViewModel = hiltViewModel()
    val context = LocalContext.current
    SettingHeader("Debug area \uD83D\uDC40")

    NavigationSetting(
        name = "Launch Showkase",
        onClick = { context.startActivity(Showkase.getBrowserIntent(context)) }
    )

    var isSuccess by remember { mutableStateOf(false) }

    val onClick: () -> Unit = {
        viewModel.insertSampleData()
        isSuccess = true
    }
    NavigationSetting(
        name = "Insert sample data" + if (isSuccess) " ✅" else "",
        onClick = onClick
    )
}