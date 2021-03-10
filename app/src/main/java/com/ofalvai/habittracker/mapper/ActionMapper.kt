package com.ofalvai.habittracker.mapper

import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.ActionHistory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity

private const val RECENT_ACTIONS_PER_HABIT = 7

fun actionsToRecentDays(actions: List<ActionEntity>): List<Action> {
    val lastDay = LocalDate.now()

    val sortedActions = actions.sortedByDescending { action -> action.timestamp }
    return (RECENT_ACTIONS_PER_HABIT - 1 downTo 0).map { i ->
        val targetDate = lastDay.minusDays(i.toLong())
        val actionOnDay = sortedActions.find { action ->
            val actionDate = LocalDateTime
                .ofInstant(action.timestamp, ZoneId.systemDefault())
                .toLocalDate()

            actionDate == targetDate
        }

        Action(
            id = actionOnDay?.id ?: 0,
            toggled = actionOnDay != null,
            actionOnDay?.timestamp
        )
    }
}

fun actionsToHistory(actions: List<ActionEntity>): ActionHistory {
    if (actions.isEmpty()) {
        return ActionHistory.Clean
    }

    val sortedActions = actions.sortedByDescending { action -> action.timestamp }
    val firstActionDate = LocalDateTime
        .ofInstant(sortedActions.first().timestamp, ZoneId.systemDefault())
        .toLocalDate()

    if (firstActionDate == LocalDate.now()) {
        // It's a streak
        var days = 0
        var previousDate = LocalDate.now().plusDays(1)
        for (action in sortedActions) {
            val actionDate = LocalDateTime
                .ofInstant(action.timestamp, ZoneId.systemDefault())
                .toLocalDate()
            if (previousDate.minusDays(1) == actionDate) {
                days++
                previousDate = previousDate.minusDays(1)
            } else {
                break
            }
        }
        return ActionHistory.Streak(days)
    } else {
        // It's a missed day streak
        var days = 0
        var previousDate = LocalDate.now()

        while (previousDate != firstActionDate) {
            days++
            previousDate = previousDate.minusDays(1)
        }

        return ActionHistory.MissedDays(days)
    }
}