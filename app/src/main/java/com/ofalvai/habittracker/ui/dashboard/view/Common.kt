package com.ofalvai.habittracker.ui.dashboard.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateHabitButton(
    onClick: () -> Unit
) {
    Box(Modifier.fillMaxWidth().wrapContentWidth()) {
        OutlinedButton(
            modifier = Modifier.padding(16.dp),
            onClick = onClick
        ) {
            Icon(Icons.Filled.Add, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Create new habit")
        }
    }
}