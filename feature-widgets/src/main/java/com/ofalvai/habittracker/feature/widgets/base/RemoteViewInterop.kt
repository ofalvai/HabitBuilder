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

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.ofalvai.habittracker.feature.widgets.GlanceTheme
import com.ofalvai.habittracker.feature.widgets.LocalGlanceMaterialColors
import com.ofalvai.habittracker.feature.widgets.R

fun Color.toColorInt(): Int {
    // This isn't 100% correct, but works with SRGB color space
    return (value shr 32).toInt()
}

/**
 * A root layout for the widget content using RemoteViews that does the following:
 * - has a correct rounded corner on all API levels (Glance Compose can't do rounded corners below
 *   Android S
 * - has a day/night background color
 * - on Android S+, the background color is the dynamic theme's background color
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
                val backgroundColor = LocalGlanceMaterialColors.current.background.toColorInt()
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

/**
 * Provide a Box composable using the system parameters for app widgets background with rounded
 * corners and background color.
 */
@Composable
fun AppWidgetBox(
    modifier: GlanceModifier = GlanceModifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
    Box(
        modifier = appWidgetBackgroundModifier().then(modifier),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * Provide a Column composable using the system parameters for app widgets background with rounded
 * corners and background color.
 */
@Composable
fun AppWidgetColumn(
    modifier: GlanceModifier = GlanceModifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = appWidgetBackgroundModifier().then(modifier),
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

/**
 * Provide a Row composable using the system parameters for app widgets background with rounded
 * corners and background color.
 */
@Composable
fun AppWidgetRow(
    modifier: GlanceModifier = GlanceModifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = appWidgetBackgroundModifier().then(modifier),
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun appWidgetBackgroundModifier() = GlanceModifier
    .fillMaxSize()
    .padding(16.dp)
    .appWidgetBackground()
    .background(GlanceTheme.colors.background)
    .appWidgetBackgroundCornerRadius()

fun GlanceModifier.appWidgetBackgroundCornerRadius(): GlanceModifier {
    if (Build.VERSION.SDK_INT >= 31) {
        cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        cornerRadius(16.dp)
    }
    return this
}

fun GlanceModifier.appWidgetInnerCornerRadius(): GlanceModifier {
    if (Build.VERSION.SDK_INT >= 31) {
        cornerRadius(android.R.dimen.system_app_widget_inner_radius)
    } else {
        cornerRadius(8.dp)
    }
    return this
}

fun GlanceModifier.clickToMainScreen(context: Context): GlanceModifier {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)!!
    return this.then(clickable(actionStartActivity(intent)))
}


@Composable
fun stringResource(@StringRes id: Int, vararg args: Any): String {
    return LocalContext.current.getString(id, args)
}

val Float.toPx get() = this * Resources.getSystem().displayMetrics.density