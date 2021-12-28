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

package com.ofalvai.habittracker.telemetry

import android.content.Context
import com.bugsnag.android.BreadcrumbType
import com.bugsnag.android.Bugsnag
import logcat.asLog
import logcat.logcat

interface Telemetry {

    enum class BreadcrumbType {
        Navigation,
        State,
        UserAction,
    }

    fun initialize()

    fun logNonFatal(e: Throwable)

    fun leaveBreadcrumb(message: String, metadata: Map<String, Any>, type: BreadcrumbType)

}

class TelemetryImpl(private val appContext: Context) : Telemetry {

    override fun initialize() {
        Bugsnag.start(appContext)
    }

    override fun logNonFatal(e: Throwable) {
        logcat { e.asLog() }
        Bugsnag.notify(e)
    }

    override fun leaveBreadcrumb(
        message: String,
        metadata: Map<String, Any>,
        type: Telemetry.BreadcrumbType
    ) {
        val bugsnagType = when (type) {
            Telemetry.BreadcrumbType.Navigation -> BreadcrumbType.NAVIGATION
            Telemetry.BreadcrumbType.State -> BreadcrumbType.STATE
            Telemetry.BreadcrumbType.UserAction -> BreadcrumbType.USER
        }
        Bugsnag.leaveBreadcrumb(message, metadata, bugsnagType)
    }

}