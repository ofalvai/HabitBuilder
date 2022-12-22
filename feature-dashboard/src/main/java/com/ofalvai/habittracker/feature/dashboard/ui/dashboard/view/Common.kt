/*
 * Copyright 2021 Olivér Falvai
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

package com.ofalvai.habittracker.feature.dashboard.ui.dashboard.view

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.common.VIBRATE_PATTERN_TOGGLE
import com.ofalvai.habittracker.core.model.HabitId
import com.ofalvai.habittracker.core.model.HabitWithActions
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import com.ofalvai.habittracker.feature.dashboard.R
import com.ofalvai.habittracker.feature.dashboard.ui.dashboard.ItemMoveEvent
import kotlinx.collections.immutable.ImmutableList
import org.burnoutcrew.reorderable.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun ReorderableHabitList(
    habits: ImmutableList<HabitWithActions>,
    verticalArrangement: Arrangement.Vertical,
    onMove: (ItemMoveEvent) -> Unit,
    onAddHabitClick: () -> Unit,
    itemContent: @Composable LazyListScope.(HabitWithActions, ReorderableLazyListState) -> Unit
) {
    val vibrator = LocalContext.current.getSystemService<Vibrator>()!!

    // An in-memory copy of the Habit list to make item reorder smooth (by avoiding recomposing
    // the entire list).
    // We update the in-memory list on every move (of distance 1), then persist to DB in the
    // background. The in-memory list is not recreated after the item order changes (triggered by
    // the DB update) thanks to the invalidation key below, but it is recreated when anything
    // other than the item order changes (such as action completion)
    val inMemoryList = remember(habits.toSet()) { habits.toMutableStateList() }
    val onItemMove: (ItemPosition, ItemPosition) -> (Unit) = { from, to ->
        vibrator.vibrateCompat(longArrayOf(0, 50))
        inMemoryList.move(from.index, to.index)
        onMove(ItemMoveEvent(from.key as HabitId, to.key as HabitId))
    }
    val canDragOver: (index: ItemPosition) -> Boolean = {
        // Last item of the list is the fixed CreateHabitButton, it's not reorderable
        it.index < inMemoryList.size
    }
    val reorderState = rememberReorderLazyListState(onMove = onItemMove, canDragOver = canDragOver)

    LazyColumn(
        state = reorderState.listState,
        contentPadding = PaddingValues(bottom = 48.dp),
        verticalArrangement = verticalArrangement,
        modifier = Modifier.reorderable(reorderState)
    ) {
        items(inMemoryList, key = { it.habit.id }) { item ->
            this@LazyColumn.itemContent(item, reorderState)
        }
        item {
            CreateHabitButton(onClick = onAddHabitClick)
        }
    }
}

@Composable
private fun CreateHabitButton(
    onClick: () -> Unit
) {
    Box(Modifier.fillMaxWidth().wrapContentWidth()) {
        OutlinedButton(
            modifier = Modifier.padding(16.dp),
            onClick = onClick,
        ) {
            Icon(Icons.Rounded.Add, null, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.dashboard_create_habit))
        }
    }
}

@Composable
fun DayLegend(
    modifier: Modifier = Modifier,
    mostRecentDay: LocalDate,
    pastDayCount: Int
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        (pastDayCount downTo 0).map {
            DayLabel(day = mostRecentDay.minusDays(it.toLong()))
        }
    }
}

@Composable
private fun DayLabel(
    day: LocalDate,
) {
    val modifier = Modifier
        .wrapContentHeight(Alignment.Top)
        .padding(vertical = 8.dp)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = day.dayOfMonth.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

fun Modifier.satisfyingToggleable(
    vibrator: Vibrator,
    rippleRadius: Dp,
    rippleBounded: Boolean,
    toggled: Boolean,
    onToggle: (Boolean) -> Unit,
    onSinglePress: () -> Unit
): Modifier {
    return composed {
        // Using `toggled` as the cache key, otherwise the recomposition in the long press
        // would leave the InteractionSource in the pressed state
        val interactionSource = remember(toggled) { MutableInteractionSource() }
        var isSinglePress by remember { mutableStateOf(false) }

        this
            .pointerInput(key1 = toggled) {
                detectTapGestures(
                    onPress = {
                        isSinglePress = true

                        vibrator.vibrateCompat(longArrayOf(0, 50))
                        val press = PressInteraction.Press(it)
                        interactionSource.emit(press)

                        val released = tryAwaitRelease()

                        if (isSinglePress) {
                            onSinglePress()
                        }
                        isSinglePress = false

                        val endInteraction = if (released) {
                            PressInteraction.Release(press)
                        } else {
                            PressInteraction.Cancel(press)
                        }
                        interactionSource.emit(endInteraction)
                    },
                    onLongPress = {
                        isSinglePress = false
                        vibrator.vibrateCompat(VIBRATE_PATTERN_TOGGLE)
                        onToggle(!toggled)
                    }
                )
            }
            .indication(interactionSource, rememberRipple(radius = rippleRadius, bounded = rippleBounded))
    }
}

fun Vibrator.vibrateCompat(timings: LongArray, repeat: Int = -1) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate(VibrationEffect.createWaveform(timings, repeat))
    } else {
        @Suppress("DEPRECATION")
        vibrate(timings, repeat)
    }
}

@Preview
@ShowkaseComposable(name = "Day labels", group = "Dashboard")
@Composable
fun PreviewDayLabels() {
    PreviewTheme {
        DayLegend(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            mostRecentDay = LocalDate.now(),
            pastDayCount = 4
        )
    }
}