package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ofalvai.habittracker.R

object Suggestions {

    val habits: List<String>
        @Composable get() = listOf(
            stringResource(R.string.habit_suggestion_meditation),
            stringResource(R.string.habit_suggestion_workout),
            stringResource(R.string.habit_suggestion_reading),
            stringResource(R.string.habit_suggestion_walk),
            stringResource(R.string.habit_suggestion_diy),
            stringResource(R.string.habit_suggestion_morning_exercise)
        )
}