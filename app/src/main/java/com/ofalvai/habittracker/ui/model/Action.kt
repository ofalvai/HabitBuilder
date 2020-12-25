package com.ofalvai.habittracker.ui.model

import java.time.Instant

data class Action(
    val id: Int,
    val timestamp: Instant,
    val toggled: Boolean
)