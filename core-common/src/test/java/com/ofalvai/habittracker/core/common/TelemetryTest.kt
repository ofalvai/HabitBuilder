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

package com.ofalvai.habittracker.core.common

import android.content.Context
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions

class TelemetryTest {

    private val context = mock<Context>()
    private val appPreferences = mock<AppPreferences>()

    @Test
    fun `Given disabled crash reporting When telemetry is initialized and used Then Bugsnag is not initialized`() {
        // Given
        given(appPreferences.crashReportingEnabled).willReturn(false)

        // When
        val telemetry = TelemetryImpl(context, appPreferences)
        telemetry.initialize()
        telemetry.logNonFatal(Throwable("Test error"))
        telemetry.leaveBreadcrumb("Test breadcrumb", emptyMap(), Telemetry.BreadcrumbType.Navigation)

        // Then
        verifyNoInteractions(context)
        // Bugsnag also crashes when calling its methods without `Bugsnag.start()` first
    }
}