/*
 * Copyright 2023 Olivér Falvai
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

package com.ofalvai.habittracker.feature.widgets.base

import android.content.res.ColorStateList
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.layout.Box
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import com.ofalvai.habittracker.feature.widgets.GlanceTheme
import com.ofalvai.habittracker.feature.widgets.LocalGlanceMaterialColors
import com.ofalvai.habittracker.feature.widgets.R

fun Color.toColorInt(): Int {
    // This isn't 100% correct, but works with SRGB color space
    return (value shr 32).toInt()
}

/**
 * A root layout for the widget content using RemoteViews that does the following:
 * - has a correct rounded corner on all API levels (Glances Compose can't do rounded corners below
 *   Android S
 * - has a day/night background color
 * - on Android S+, the background color is the dynamic theme's surfaceVariant color
 * - the root of the layout has the android.R.id.background ID for smooth transitions
 */
@Composable
fun AppWidgetRoot(
    modifier: GlanceModifier = GlanceModifier,
    content: @Composable () -> Unit
) {
    GlanceTheme {
        Box(
            GlanceModifier
                .fillMaxSize()
                .then(modifier)
        ) {
            val remoteViews = RemoteViews(LocalContext.current.packageName, R.layout.app_widget_root)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val backgroundColor = LocalGlanceMaterialColors.current.surfaceVariant.toColorInt()
                remoteViews.setColorStateList(R.id.app_widget_background, "setImageTintList", ColorStateList.valueOf(backgroundColor))
            }
            // On lower API levels we take the day/night background colors defined in colors.xml


            AndroidRemoteViews(
                remoteViews = remoteViews,
                containerViewId = R.id.app_widget_container,
                content = {
                    // Emitting the content composable here doesn't work for some reason, so we
                    // just stretch this view and draw the content on top of it
                    Spacer(GlanceModifier.fillMaxSize())
                })

            content()
        }
    }
}