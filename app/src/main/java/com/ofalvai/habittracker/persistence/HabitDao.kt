package com.ofalvai.habittracker.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ofalvai.habittracker.persistence.entity.Action
import com.ofalvai.habittracker.persistence.entity.Habit
import java.time.Instant

@Dao
interface HabitDao {

    @Query("SELECT * FROM habit")
    suspend fun getHabits(): List<Habit>

    @Insert
    suspend fun insertHabit(vararg habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM `action` WHERE habit_id = :habitId")
    suspend fun getActionsForHabit(habitId: Int): List<Action>

    @Query("SELECT * FROM `action` WHERE timestamp >= :after")
    suspend fun getActionsAfter(after: Instant): List<Action>

    @Insert
    suspend fun insertAction(vararg action: Action)

    @Delete
    suspend fun deleteAction(action: Action)
}