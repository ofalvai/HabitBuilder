package com.ofalvai.habittracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.mapHabitEntityToModel
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.HabitWithActions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

class DashboardViewModel(
    private val dao: HabitDao,
    appPreferences: AppPreferences
) : ViewModel() {

    val habitsWithActions: Flow<List<HabitWithActions>> = dao
        .getHabitsWithActions()
        .distinctUntilChanged()
        .map(::mapHabitEntityToModel)

    var dashboardConfig by appPreferences::dashboardConfig

    fun toggleActionFromDashboard(habitId: Int, action: Action, date: LocalDate) {
        viewModelScope.launch {
            this@DashboardViewModel.toggleAction(habitId, action, date)
        }
    }

    // TODO: duplicated across Dashboard + HabitDetails
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