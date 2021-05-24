package com.ofalvai.habittracker.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.*
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.common.SingleLiveEvent
import com.ofalvai.habittracker.ui.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.*
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity
import com.ofalvai.habittracker.persistence.entity.Habit as HabitEntity

class HabitViewModel(
    private val dao: HabitDao,
    appPreferences: AppPreferences
) : ViewModel() {

    val habitsWithActions = Transformations.map(
        Transformations.distinctUntilChanged(dao.getHabitsWithActions()),
        ::mapHabitEntityToModel
    )

    val habitWithActions = MutableLiveData<HabitWithActions?>()
    val singleStats = MutableLiveData<SingleStats>()
    val actionCountByWeek = MutableLiveData<List<ActionCountByWeek>>()
    val actionCountByMonth = MutableLiveData<List<ActionCountByMonth>>()
    val backNavigationEvent = SingleLiveEvent<Void>()

    var dashboardConfig by appPreferences::dashboardConfig

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            val habitEntity = HabitEntity(name = habit.name, color = habit.color.toEntityColor())
            dao.insertHabit(habitEntity)
            backNavigationEvent.call()
        }
    }

    fun toggleActionFromDashboard(habitId: Int, action: Action, date: LocalDate) {
        viewModelScope.launch {
            toggleAction(habitId, action, date)
        }
    }

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
            habitWithActions.value = habit
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
}