package com.ofalvai.habittracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ofalvai.habittracker.ui.Screen

@Composable
fun AboutScreen(navController: NavController) {

    Column(
        Modifier.padding(32.dp).verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = { navController.navigate(Screen.Licenses.route) }
        ) {
            Text(text = "Open-source libraries")
        }
    }
}