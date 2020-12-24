package com.ofalvai.habittracker.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity
data class Action(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habit_id: Int,
    val timestamp: Instant
)