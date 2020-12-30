package com.ofalvai.habittracker.ui.habitdetail

import androidx.lifecycle.ViewModel
import com.ofalvai.habittracker.persistence.HabitDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class HabitDetailViewModel(
    private val habitDao: HabitDao,
    private val coroutineScope: CoroutineScope,
) : ViewModel() {

    fun fetchActions(habitId: Int) {
        // TODO: When to instantiate VM? How to get ID? What to do inside the Composable?
        coroutineScope.launch {
            habitDao.getActionsForHabit(habitId).map {
                Timber.d(it.toString())
            }
        }
    }

}