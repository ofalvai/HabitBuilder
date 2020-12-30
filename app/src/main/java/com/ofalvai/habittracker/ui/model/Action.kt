package com.ofalvai.habittracker.ui.model

import java.time.Instant

data class Action(
    val id: Int,
    val toggled: Boolean,
    val timestamp: Instant?
)