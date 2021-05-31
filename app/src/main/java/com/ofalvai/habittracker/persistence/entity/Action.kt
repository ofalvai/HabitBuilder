package com.ofalvai.habittracker.persistence.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = CASCADE
        )
    ]
)
data class Action(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habit_id: Int,
    val timestamp: Instant
)