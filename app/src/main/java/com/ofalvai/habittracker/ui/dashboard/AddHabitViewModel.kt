package com.ofalvai.habittracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.toEntityColor
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.common.SingleLiveEvent
import com.ofalvai.habittracker.ui.model.Habit
import kotlinx.coroutines.launch

class AddHabitViewModel(
    private val dao: HabitDao,
) : ViewModel() {

    val backNavigationEvent = SingleLiveEvent<Void>()

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            val habitEntity = com.ofalvai.habittracker.persistence.entity.Habit(
                name = habit.name,
                color = habit.color.toEntityColor()
            )
            dao.insertHabit(habitEntity)
            backNavigationEvent.call()
        }
    }
}