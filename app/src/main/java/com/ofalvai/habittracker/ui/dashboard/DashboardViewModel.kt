package com.ofalvai.habittracker.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity
import com.ofalvai.habittracker.persistence.entity.Habit as HabitEntity

class DashboardViewModel(
    private val dao: HabitDao,
    private val coroutineScope: CoroutineScope
): ViewModel() {

    val habitsWithActions = MutableLiveData<List<HabitWithActions>>()

    init {
        loadHabitsWithHistory()
    }

    fun addHabit(habit: Habit) {
        coroutineScope.launch {
            val habitEntity = HabitEntity(name = habit.name)
            dao.insertHabit(habitEntity)

            loadHabitsWithHistory()
        }
    }

    fun toggleAction(action: Action, habit: Habit, dayIndex: Int) {
        coroutineScope.launch {
            if (!action.toggled) {
                dao.deleteAction(action.id)
            } else {
                val newAction = ActionEntity(
                    habit_id = habit.id,
                    timestamp = Instant.now().minus((4 - dayIndex).toLong(), ChronoUnit.DAYS)
                )
                dao.insertAction(newAction)
            }

            loadHabitsWithHistory()
        }
    }

    private fun loadHabitsWithHistory() {
        coroutineScope.launch {
            habitsWithActions.value = dao.getHabitsWithActions().map {
                HabitWithActions(
                    Habit(it.habit.id, it.habit.name),
                    actionsToRecentDays(it.actions)
                )
            }
        }
    }

    private fun actionsToRecentDays(actions: List<ActionEntity>): List<Action> {
        val lastDay = LocalDate.now()

        val sortedActions = actions.sortedByDescending { action -> action.timestamp }
        return (4 downTo 0).map { i ->
            val targetDate = lastDay.minusDays(i.toLong())
            val actionOnDay = sortedActions.find { action ->
                val actionDate = LocalDateTime
                    .ofInstant(action.timestamp, ZoneId.systemDefault())
                    .toLocalDate()

                actionDate == targetDate
            }

            Action(id = actionOnDay?.id ?: 0, toggled = actionOnDay != null)
        }
    }
}
