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

package com.ofalvai.habittracker.ui.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ofalvai.habittracker.Dependencies

@Composable
fun ExportScreen(navController: NavController) {
    val viewModel = viewModel<ExportViewModel>(factory = Dependencies.viewModelFactory)

    val launcher = rememberLauncherForActivityResult(viewModel.getCreateDocumentContract()) {
        viewModel.onCreateDocumentResult(it)
    }

    Column(modifier = Modifier.statusBarsPadding()) {
        Button(onClick = { launcher.launch(viewModel.getDocumentName()) }) {
            Text(text = "Export data")
        }
    }
}

