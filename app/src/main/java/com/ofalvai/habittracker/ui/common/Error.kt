package com.ofalvai.habittracker.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.theme.AppIcons
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme

@Composable
fun ErrorView(
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = AppIcons.Error,
            contentDescription = stringResource(R.string.common_error)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.body1
        )
    }
}

@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewErrorView() {
    HabitTrackerTheme {
        Column {
            ErrorView(label = "Failed to load habits.")
            ErrorView(label = "Failed to add new habit. Please try again.")
        }
    }
}