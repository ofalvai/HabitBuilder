package com.ofalvai.habittracker.ui.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.*
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.common.SingleLiveEvent
import com.ofalvai.habittracker.ui.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.*

private val initialSingleStats = SingleStats(null, 0, 0, 0f)

class HabitDetailViewModel(
    private val dao: HabitDao
) : ViewModel() {

    val habitWithActions = MutableStateFlow<Result<HabitWithActions>>(Result.Loading)
    val singleStats = MutableStateFlow(initialSingleStats)
    val actionCountByWeek = MutableStateFlow<List<ActionCountByWeek>>(emptyList())
    val actionCountByMonth = MutableStateFlow<List<ActionCountByMonth>>(emptyList())
    val backNavigationEvent = SingleLiveEvent<Void>()

    fun fetchHabitDetails(habitId: Int): Job {
        return viewModelScope.launch {
            val habit = dao.getHabitWithActions(habitId).let {
                // TODO: unify this with the regular mapping (where empty day action are filled)
                HabitWithActions(
                    Habit(it.habit.id, it.habit.name, it.habit.color.toUIColor()),
                    it.actions.map { action ->
                        Action(action.id, toggled = true, timestamp = action.timestamp)
                    },
                    it.actions.size,
                    actionsToHistory(it.actions)
                )
            }
            habitWithActions.value = Result.Success(habit)
        }
    }

    fun fetchHabitStats(habitId: Int): Job {
        // TODO: parallel execution
        return viewModelScope.launch {
            val completionRate = dao.getCompletionRate(habitId)
            val actionCountByWeekEntity = dao.getActionCountByWeek(habitId)
            val actionCountByMonthEntity = dao.getActionCountByMonth(habitId)

            singleStats.value = mapHabitSingleStats(
                completionRate,
                actionCountByWeekEntity,
                LocalDate.now(),
                Locale.getDefault()
            )
            actionCountByWeek.value = mapActionCountByWeek(actionCountByWeekEntity)
            actionCountByMonth.value = mapActionCountByMonth(actionCountByMonthEntity)
        }
    }

    fun toggleActionFromDetail(habitId: Int, action: Action, date: LocalDate) {
        viewModelScope.launch {
            toggleAction(habitId, action, date)
            fetchHabitDetails(habitId)
            fetchHabitStats(habitId)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            dao.updateHabit(habit.toEntity())
            fetchHabitDetails(habit.id)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            dao.deleteHabit(habit.toEntity())
            backNavigationEvent.call()
        }
    }

    private suspend fun toggleAction(
        habitId: Int,
        updatedAction: Action,
        date: LocalDate,
    ) {
        if (updatedAction.toggled) {
            val newAction = com.ofalvai.habittracker.persistence.entity.Action(
                habit_id = habitId,
                timestamp = LocalDateTime.of(date, LocalTime.now())
                    .toInstant(OffsetDateTime.now().offset)
            )
            dao.insertAction(newAction)
        } else {
            dao.deleteAction(updatedAction.id)
        }
    }

}