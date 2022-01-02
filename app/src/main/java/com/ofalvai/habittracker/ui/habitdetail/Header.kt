/*
 * Copyright 2022 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ofalvai.habittracker.ui.habitdetail

import androidx.annotation.FloatRange
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.common.ErrorView
import com.ofalvai.habittracker.ui.common.HabitColorPicker
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import com.ofalvai.habittracker.ui.model.SingleStats
import com.ofalvai.habittracker.ui.theme.*
import kotlin.math.roundToInt

@Composable
internal fun HabitDetailHeader(
    habitDetailState: Result<HabitWithActions>,
    singleStats: SingleStats,
    onBack: () -> Unit,
    onSave: (Habit) -> Unit,
    onArchive: (Habit) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when (habitDetailState) {
            is Result.Success -> {
                if (isEditing) MaterialTheme.colors.surface else {
                    if (MaterialTheme.colors.isLight) {
                        habitDetailState.value.habit.color.composeColor.copy(alpha = 0.4f)
                    } else MaterialTheme.colors.surfaceVariant
                }
            }
            else -> MaterialTheme.colors.background
        },
        animationSpec = tween(durationMillis = 900)
    )

    Surface(color = backgroundColor) {
        when (habitDetailState) {
            Result.Loading -> HabitDetailLoadingAppBar(onBack)
            is Result.Failure -> {
                ErrorView(
                    label = stringResource(R.string.habitdetails_error),
                    modifier = Modifier.statusBarsPadding()
                )
            }
            is Result.Success -> {
                AnimatedVisibility(
                    visible = isEditing,
                    enter = fadeIn() + expandVertically(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    HabitHeaderEditingContent(
                        habitDetails = habitDetailState.value,
                        onBack = onBack,
                        onSave = {
                            isEditing = false
                            onSave(it)
                        },
                        onArchive = onArchive
                    )
                }

                AnimatedVisibility(visible = !isEditing, enter = fadeIn(), exit = fadeOut()) {
                    HabitHeaderContent(
                        habitDetails = habitDetailState.value,
                        singleStats = singleStats,
                        onBack = onBack,
                        onEdit = { isEditing = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitHeaderEditingContent(
    habitDetails: HabitWithActions,
    onBack: () -> Unit,
    onSave: (Habit) -> Unit,
    onArchive: (Habit) -> Unit
) {
    var editingName by remember(habitDetails.habit.name) {
        mutableStateOf(habitDetails.habit.name)
    }
    var editingNotes by remember(habitDetails.habit.notes) {
        mutableStateOf(habitDetails.habit.notes)
    }
    var editingColor by remember(habitDetails.habit.color) {
        mutableStateOf(habitDetails.habit.color)
    }
    var isNameValid by remember { mutableStateOf(true) }
    val onSaveClick = {
        if (isNameValid) {
            val newValue = habitDetails.habit.copy(
                name = editingName, color = editingColor, notes = editingNotes
            )
            onSave(newValue)
        }
    }

    Column(
        modifier = Modifier
            .padding(bottom = 32.dp)
            .statusBarsPadding()
    ) {
        HabitDetailEditingAppBar(
            onBack = onBack,
            onSave = onSaveClick,
            onArchive = { onArchive(habitDetails.habit) }
        )
        OutlinedTextField(
            value = editingName,
            onValueChange = {
                editingName = it
                isNameValid = it.isNotBlank()
            },
            label = { Text(stringResource(R.string.habitdetails_edit_name_label)) },
            modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
            isError = !isNameValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = editingNotes,
            onValueChange = { editingNotes = it },
            label = { Text(stringResource(R.string.habitdetails_edit_notes_label)) },
            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 16.dp).fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.None
            )
        )
        HabitColorPicker(
            initialColor = habitDetails.habit.color,
            onColorPick = { editingColor = it }
        )
    }
}

@Composable
private fun HabitHeaderContent(
    habitDetails: HabitWithActions,
    singleStats: SingleStats,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        Modifier
            .padding(bottom = 32.dp)
            .statusBarsPadding()
    ) {
        HabitDetailAppBar(
            onBack = onBack,
            onEdit = onEdit,
        )
        Text(
            text = habitDetails.habit.name,
            modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
            style = AppTextStyle.habitTitle,
            textAlign = TextAlign.Center
        )
        if (habitDetails.habit.notes.isNotBlank()) {
            Text(
                text = habitDetails.habit.notes,
                modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
        SingleStatRow(
            totalCount = singleStats.actionCount,
            weeklyCount = singleStats.weeklyActionCount,
            completionRate = singleStats.completionRate
        )
    }
}

@Composable
private fun SingleStatRow(
    totalCount: Int,
    weeklyCount: Int,
    @FloatRange(from = 0.0, to = 1.0) completionRate: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SingleStat(
            value = totalCount.toString(),
            label = stringResource(R.string.habitdetails_singlestat_total),
            modifier = Modifier.weight(0.33f)
        )
        SingleStat(
            value = weeklyCount.toString(),
            label = stringResource(R.string.habitdetails_singlestat_weekly),
            modifier = Modifier.weight(0.33f)
        )
        SingleStat(
            value = (completionRate * 100).roundToInt().toString() + "%",
            label = stringResource(R.string.habitdetails_singlestat_completionrate),
            modifier = Modifier.weight(0.33f)
        )
    }
}

@Composable
private fun SingleStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = value,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = AppTextStyle.singleStatValue
        )
        Text(
            text = label,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HabitDetailAppBar(
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
            }
        },
        actions = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, stringResource(R.string.common_edit))
                }
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@Composable
private fun HabitDetailEditingAppBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onArchive: () -> Unit
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
            }
        },
        actions = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                IconButton(onClick = onSave) {
                    Icon(Icons.Rounded.Check, stringResource(R.string.common_save))
                }
                IconButton(onClick = onArchive) {
                    Icon(AppIcons.Archive, stringResource(R.string.common_archive))
                }
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@Composable
private fun HabitDetailLoadingAppBar(onBack: () -> Unit) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back))
            }
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PreviewSingleStats() {
    HabitTrackerTheme {
        SingleStatRow(totalCount = 18, weeklyCount = 2, completionRate = 0.423555f)
    }
}