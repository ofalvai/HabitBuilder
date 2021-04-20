package com.ofalvai.habittracker.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ofalvai.habittracker.persistence.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

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

    @Query("SELECT count(*) FROM habit")
    fun getHabitCount(): Flow<Int>

    // TODO: limit by timestamp
    @Transaction
    @Query("SELECT * FROM habit")
    fun getHabitsWithActions(): LiveData<List<HabitWithActions>>

    @Transaction
    @Query("SELECT * FROM habit WHERE id = :habitId")
    suspend fun getHabitWithActions(habitId: Int): HabitWithActions

    @Query("SELECT * FROM `action` WHERE habit_id = :habitId")
    suspend fun getActionsForHabit(habitId: Int): List<Action>

    @Query("SELECT * FROM `action` WHERE timestamp >= :after")
    suspend fun getActionsAfter(after: Instant): List<Action>

    @Insert
    suspend fun insertAction(vararg action: Action)

    @Query("DELETE FROM `action` WHERE id = :id")
    suspend fun deleteAction(id: Int)

    /**
     * When there are no actions returns UNIX epoch time as first_day and action_count of 0
     */
    @Query(
        """SELECT
                min(timestamp) as first_day,
                count(*) as action_count
            FROM `action`
            WHERE habit_id = :habitId
    """
    )
    suspend fun getCompletionRate(habitId: Int): ActionCompletionRate

    @Query(
        """SELECT 
                strftime('%Y', timestamp / 1000, 'unixepoch', 'localtime') as year,
                strftime('%m', timestamp / 1000, 'unixepoch', 'localtime') as month,
                count(*) as action_count
            FROM `action`
            WHERE habit_id = :habitId
            GROUP BY year, month"""
    )
    suspend fun getActionCountByMonth(habitId: Int): List<ActionCountByMonth>

    // Week of year calculation explanation: https://stackoverflow.com/a/15511864/745637
    @Query(
        """SELECT
                strftime('%Y', date(timestamp / 1000, 'unixepoch', 'localtime', '-3 days', 'weekday 4')) as year,
                (strftime('%j', date(timestamp / 1000, 'unixepoch', 'localtime', '-3 days', 'weekday 4')) - 1) / 7 + 1 as week,
                count(*) as action_count
            FROM `action`
            WHERE habit_id = :habitId
            GROUP BY year, week"""
    )
    suspend fun getActionCountByWeek(habitId: Int): List<ActionCountByWeek>

    @Query(
        """SELECT
                date(timestamp / 1000, 'unixepoch', 'localtime') as date,
                count(*) AS action_count
            FROM `action`
            WHERE date >= date(:from) AND date <= date(:to)
            GROUP BY date
        """
    )
    suspend fun getSumActionCountByDay(from: LocalDate, to:LocalDate): List<SumActionCountByDay>

    @Query(
        """SELECT
                habit.id AS habit_id,
                habit.name AS name,
                date(min(`action`.timestamp) / 1000, 'unixepoch', 'localtime') as first_day,
                count(habit_id) AS count
            FROM habit
            LEFT JOIN `action` ON habit.id = `action`.habit_id
            GROUP BY habit.id
            ORDER BY count DESC
            LIMIT :count"""
    )
    suspend fun getMostSuccessfulHabits(count: Int): List<HabitActionCount>

    @Query(
        """SELECT
                id as habit_id,
                name,
                (
                    SELECT strftime('%w', `action`.timestamp / 1000, 'unixepoch', 'localtime') as day_of_week
                    FROM `action`
                    WHERE habit_id = habit.id
                    GROUP BY day_of_week
                    ORDER BY count(*) DESC
                    LIMIT 1
                ) as top_day_of_week,
                (
                    SELECT count(*) as count
                    FROM `action`
                    WHERE habit_id = habit.id
                    GROUP BY strftime('%w', `action`.timestamp / 1000, 'unixepoch', 'localtime')
                    ORDER BY count DESC
                    LIMIT 1
                ) as action_count_on_day
            FROM habit"""
    )
    suspend fun getTopDayForHabits(): List<HabitTopDay>
}