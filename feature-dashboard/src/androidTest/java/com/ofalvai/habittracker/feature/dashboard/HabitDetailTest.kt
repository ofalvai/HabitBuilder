package com.ofalvai.habittracker.feature.dashboard

import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.ofalvai.habittracker.core.model.ActionHistory
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.core.testing.BaseInstrumentedTest
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.feature.dashboard.ui.habitdetail.HabitDetailHeader
import com.ofalvai.habittracker.feature.dashboard.ui.model.SingleStats
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HabitDetailTest : BaseInstrumentedTest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenHabitWithTime_WhenViewingDetails_ThenTimeIsDisplayed() {
        // Given
        val habitTime = LocalTime.of(14, 30)
        val habit = Habit(
            id = 1,
            name = "Test Habit",
            color = Habit.Color.Blue,
            notes = "Test Notes",
            time = habitTime
        )
        val habitWithActions = HabitWithActions(
            habit = habit,
            actions = persistentListOf(),
            totalActionCount = 0,
            actionHistory = ActionHistory.Clean
        )
        val singleStats = SingleStats(
            firstDay = null,
            actionCount = 0,
            weeklyActionCount = 0,
            completionRate = 0f
        )

        // When
        composeTestRule.setContent {
            HabitDetailHeader(
                habitDetailState = Result.Success(habitWithActions),
                singleStats = singleStats,
                scrollState = rememberScrollState(),
                onBack = {},
                onSave = {},
                onArchive = {}
            )
        }

        // Then
        val expectedTimeText = habitTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        composeTestRule.onNodeWithText(expectedTimeText).assertIsDisplayed()
    }
}
