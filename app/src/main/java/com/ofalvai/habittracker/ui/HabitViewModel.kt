package com.ofalvai.habittracker.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.*
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity
import com.ofalvai.habittracker.persistence.entity.Habit as HabitEntity
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

private const val ACTION_DAYS_TO_FETCH = 7

class HabitViewModel(
    private val dao: HabitDao,
    private val coroutineScope: CoroutineScope,
    appPreferences: AppPreferences
) : ViewModel() {

    val habitsWithActions = Transformations.map(
        Transformations.distinctUntilChanged(dao.getHabitsWithActions()),
        this::mapHabitEntityToModel
    )

    val habitWithActions = MutableLiveData<HabitWithActions?>()

    var dashboardConfig by appPreferences::dashboardConfig

    fun addHabit(habit: Habit) {
        coroutineScope.launch {
            val habitEntity = HabitEntity(name = habit.name, color = habit.color.toEntityColor())
            dao.insertHabit(habitEntity)
        }
    }

    fun toggleActionFromDashboard(habitId: Int, action: Action, date: LocalDate) {
        coroutineScope.launch {
            toggleAction(habitId, action, date)
        }
    }

    fun fetchHabitDetails(habitId: Int): Job {
        // Clear previous result (for a possibly different habit ID)
        habitWithActions.value = null

        return coroutineScope.launch {
            val habit = dao.getHabitWithActions(habitId).let {
                HabitWithActions(
                    Habit(it.habit.id, it.habit.name, it.habit.color.toUIColor()),
                    it.actions.map { action ->
                        Action(action.id, toggled = true, timestamp = action.timestamp)
                    },
                    it.actions.size
                )
            }
            habitWithActions.value = habit
        }
    }

    fun toggleActionFromDetail(habitId: Int, action: Action, date: LocalDate) {
        coroutineScope.launch {
            toggleAction(habitId, action, date)
            fetchHabitDetails(habitId)
        }
    }

    fun updateHabit(habit: Habit) {
        coroutineScope.launch {
            dao.updateHabit(
                HabitEntity(
                    id = habit.id,
                    name = habit.name,
                    color = habit.color.toEntityColor()
                )
            )
            fetchHabitDetails(habit.id)
        }
    }

    fun deleteHabit(habit: Habit) {
        coroutineScope.launch {
            dao.deleteHabit(HabitEntity(
                id = habit.id,
                name = habit.name,
                color = habit.color.toEntityColor()
            ))
        }
    }

    private suspend fun toggleAction(
        habitId: Int,
        updatedAction: Action,
        date: LocalDate,
    ) {
        if (updatedAction.toggled) {
            val newAction = ActionEntity(
                habit_id = habitId,
                timestamp = LocalDateTime.of(date, LocalTime.now())
                    .toInstant(OffsetDateTime.now().offset)
            )
            dao.insertAction(newAction)
        } else {
            dao.deleteAction(updatedAction.id)
        }
    }

    private fun mapHabitEntityToModel(habitsWithActions: List<HabitWithActionsEntity>): List<HabitWithActions> {
        return habitsWithActions.map {
            HabitWithActions(
                Habit(it.habit.id, it.habit.name, it.habit.color.toUIColor()),
                actionsToRecentDays(it.actions),
                totalActionCount = it.actions.size
            )
        }
    }

    private fun actionsToRecentDays(actions: List<ActionEntity>): List<Action> {
        val lastDay = LocalDate.now()

        val sortedActions = actions.sortedByDescending { action -> action.timestamp }
        return (ACTION_DAYS_TO_FETCH - 1 downTo 0).map { i ->
            val targetDate = lastDay.minusDays(i.toLong())
            val actionOnDay = sortedActions.find { action ->
                val actionDate = LocalDateTime
                    .ofInstant(action.timestamp, ZoneId.systemDefault())
                    .toLocalDate()

                actionDate == targetDate
            }

            Action(id = actionOnDay?.id ?: 0, toggled = actionOnDay != null, actionOnDay?.timestamp)
        }
    }
}

fun HabitEntity.Color.toUIColor(): Habit.Color = when (this) {
    HabitEntity.Color.Red -> Habit.Color.Red
    HabitEntity.Color.Green -> Habit.Color.Green
    HabitEntity.Color.Blue -> Habit.Color.Blue
    HabitEntity.Color.Yellow -> Habit.Color.Yellow
}

fun Habit.Color.toEntityColor(): HabitEntity.Color = when (this) {
    Habit.Color.Red -> HabitEntity.Color.Red
    Habit.Color.Green -> HabitEntity.Color.Green
    Habit.Color.Blue -> HabitEntity.Color.Blue
    Habit.Color.Yellow -> HabitEntity.Color.Yellow
}