package com.ofalvai.habittracker.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)