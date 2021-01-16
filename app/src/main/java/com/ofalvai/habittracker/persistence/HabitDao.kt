package com.ofalvai.habittracker.persistence

import androidx.room.*
import com.ofalvai.habittracker.persistence.entity.Action
import com.ofalvai.habittracker.persistence.entity.Habit
import com.ofalvai.habittracker.persistence.entity.HabitWithActions
import java.time.Instant

@Dao
interface HabitDao {

    @Query("SELECT * FROM habit")
    suspend fun getHabits(): List<Habit>

    @Insert
    suspend fun insertHabit(vararg habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    // TODO: limit by timestamp
    @Transaction
    @Query("SELECT * FROM habit")
    suspend fun getHabitsWithActions(): List<HabitWithActions>

    @Query("SELECT * FROM `action` WHERE habit_id = :habitId")
    suspend fun getActionsForHabit(habitId: Int): List<Action>

    @Query("SELECT * FROM `action` WHERE timestamp >= :after")
    suspend fun getActionsAfter(after: Instant): List<Action>

    @Insert
    suspend fun insertAction(vararg action: Action)

    @Query("DELETE FROM `action` WHERE id = :id")
    suspend fun deleteAction(id: Int)
}