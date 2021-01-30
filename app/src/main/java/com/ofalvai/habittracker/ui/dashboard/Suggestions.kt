package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.model.Habit

object Suggestions {

    val habits: List<Habit>
        @Composable get() = listOf(
            Habit(name = stringResource(R.string.habit_suggestion_meditation), color = Habit.Color.Red),
            Habit(name = stringResource(R.string.habit_suggestion_workout), color = Habit.Color.Blue),
            Habit(name = stringResource(R.string.habit_suggestion_reading), color = Habit.Color.Green),
            Habit(name = stringResource(R.string.habit_suggestion_walk), color = Habit.Color.Green),
            Habit(name = stringResource(R.string.habit_suggestion_diy), color = Habit.Color.Red),
            Habit(name = stringResource(R.string.habit_suggestion_morning_exercise), color = Habit.Color.Blue)
        )

}