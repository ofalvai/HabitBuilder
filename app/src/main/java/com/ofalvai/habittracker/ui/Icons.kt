package com.ofalvai.habittracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.ofalvai.habittracker.R

object AppIcons {

    val Habits: Painter
        @Composable
        get() = painterResource(R.drawable.ic_habit)

    val Insights: Painter
        @Composable
        get() = painterResource(R.drawable.ic_insights)

    val DashboardLayout: Painter
        @Composable
        get() = painterResource(R.drawable.ic_dashboard_layout)
}

