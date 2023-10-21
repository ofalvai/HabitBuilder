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

package com.ofalvai.habittracker.feature.dashboard.ui.habitdetail

import androidx.annotation.FloatRange
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.model.Habit
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.core.ui.component.ErrorView
import com.ofalvai.habittracker.core.ui.component.HabitColorPicker
import com.ofalvai.habittracker.core.ui.component.SingleStat
import com.ofalvai.habittracker.core.ui.state.Result
import com.ofalvai.habittracker.core.ui.theme.*
import com.ofalvai.habittracker.feature.dashboard.R
import com.ofalvai.habittracker.feature.dashboard.ui.model.SingleStats
import kotlin.math.roundToInt
import com.ofalvai.habittracker.core.ui.R as coreR

private const val SCROLL_COLLAPSE_THRESHOLD = 10

@Composable
internal fun HabitDetailHeader(
    habitDetailState: Result<HabitWithActions>,
    singleStats: SingleStats,
    scrollState: ScrollState,
    onBack: () -> Unit,
    onSave: (Habit) -> Unit,
    onArchive: (Habit) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when (habitDetailState) {
            is Result.Success -> {
                if (isEditing) MaterialTheme.colorScheme.surface else {
                    habitDetailState.value.habit.color.composeContainerColor
                }
            }
            else -> MaterialTheme.colorScheme.background
        },
        animationSpec = tween(durationMillis = 900, delayMillis = 150), label = "HabitDetailHeader"
    )

    Surface(color = backgroundColor) {
        when (habitDetailState) {
            Result.Loading -> HabitDetailLoadingAppBar(onBack)
            is Result.Failure -> {
                ErrorView(label = stringResource(R.string.habitdetails_error))
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
                        scrollState = scrollState,
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

    Column(Modifier.padding(bottom = 32.dp)) {
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
            color = editingColor,
            onColorPick = { editingColor = it }
        )
    }
}

@Composable
private fun HabitHeaderContent(
    habitDetails: HabitWithActions,
    singleStats: SingleStats,
    scrollState: ScrollState,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val bottomPadding by animateDpAsState(targetValue = if (scrollState.value < SCROLL_COLLAPSE_THRESHOLD) 32.dp else 8.dp,
        label = "HeaderContent"
    )
    Column(Modifier.padding(bottom = bottomPadding)) {
        val contentColor = habitDetails.habit.color.composeOnContainerColor
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            HabitDetailAppBar(habitDetails, scrollState, onBack, onEdit)
            AnimatedVisibility(visible = scrollState.value < SCROLL_COLLAPSE_THRESHOLD) {
                Column(Modifier.padding(bottom = 32.dp)) {
                    Text(
                        text = habitDetails.habit.name,
                        modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
                        style = AppTextStyle.habitDisplay,
                        textAlign = TextAlign.Center
                    )
                    if (habitDetails.habit.notes.isNotBlank()) {
                        Text(
                            text = habitDetails.habit.notes,
                            modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            SingleStatRow(
                totalCount = singleStats.actionCount,
                weeklyCount = singleStats.weeklyActionCount,
                completionRate = singleStats.completionRate,
            )
        }
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
            .padding(start = 32.dp, end = 16.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitDetailAppBar(
    habitDetails: HabitWithActions,
    scrollState: ScrollState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    TopAppBar(
        title = {
            AnimatedVisibility(
                visible = scrollState.value > SCROLL_COLLAPSE_THRESHOLD,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = habitDetails.habit.name,
                    style = AppTextStyle.screenTitle
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) { TopAppBarNavIcon() }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, stringResource(coreR.string.common_edit))
            }
        },
        colors = topAppBarColors(habitDetails.habit.color),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitDetailEditingAppBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onArchive: () -> Unit
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) { TopAppBarNavIcon() }
        },
        actions = {
            IconButton(onClick = onSave) {
                Icon(Icons.Rounded.Check, stringResource(coreR.string.common_save))
            }
            IconButton(onClick = onArchive) {
                Icon(CoreIcons.Archive, stringResource(coreR.string.common_archive))
            }
        },
        colors = topAppBarColors(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitDetailLoadingAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) { TopAppBarNavIcon() }
        },
        colors = topAppBarColors(),
    )
}

@Composable
private fun TopAppBarNavIcon() = Icon(Icons.Rounded.ArrowBack, stringResource(coreR.string.common_back))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topAppBarColors(habitColor: Habit.Color? = null): TopAppBarColors {
    val contentColor = habitColor?.composeOnContainerColor ?: MaterialTheme.colorScheme.onSurface
    return TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        actionIconContentColor = contentColor,
        navigationIconContentColor = contentColor
    )
}

@Preview
@ShowkaseComposable(name = "Single stats", group = "Habit details")
@Composable
fun PreviewSingleStats() {
    PreviewTheme {
        SingleStatRow(totalCount = 18, weeklyCount = 2, completionRate = 0.423555f)
    }
}