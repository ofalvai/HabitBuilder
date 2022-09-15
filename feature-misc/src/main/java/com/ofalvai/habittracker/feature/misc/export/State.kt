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

package com.ofalvai.habittracker.feature.misc.export

import androidx.compose.runtime.Immutable
import java.net.URI
import java.time.Instant

@Immutable
data class DataSummary(
    val habitCount: Int,
    val actionCount: Int,
    val lastActivity: Instant? // null if there are no actions
)

enum class ExportImportError {
    FilePickerURIEmpty,
    ExportFailed,
    ImportFailed,
    BackupVersionTooHigh
}

@Immutable
data class ExportState(
    val outputFileURI: URI?,
    val error: ExportImportError?
)

@Immutable
data class ImportState(
    val backupFileURI: URI?,
    val backupSummary: DataSummary?,
    val error: ExportImportError?
)