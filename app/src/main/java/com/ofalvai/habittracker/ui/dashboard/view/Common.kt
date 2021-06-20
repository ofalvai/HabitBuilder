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

package com.ofalvai.habittracker.ui.dashboard.view

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun CreateHabitButton(
    onClick: () -> Unit
) {
    Box(Modifier.fillMaxWidth().wrapContentWidth()) {
        OutlinedButton(
            modifier = Modifier.padding(16.dp),
            onClick = onClick
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
fun DayLabel(
    day: LocalDate,
) {
    val modifier = Modifier
        .wrapContentHeight(Alignment.Top)
        .padding(vertical = 8.dp)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = day.dayOfMonth.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption
        )
        Text(
            text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewDayLabels() {
    HabitTrackerTheme {
        DayLegend(
            modifier = Modifier.padding(horizontal = 16.dp),
            mostRecentDay = LocalDate.now(),
            pastDayCount = 4
        )
    }
}

// TODO: broken after beta-08 -> rewrite or wait for a fix
// Most likely broken by https://android-review.googlesource.com/c/platform/frameworks/support/+/1714106
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
                        vibrator.vibrateCompat(longArrayOf(0, 75, 50, 75))
                        onToggle(!toggled)
                    }
                )
            }
            .indication(interactionSource, rememberRipple(radius = rippleRadius, bounded = rippleBounded))
    }
}

private fun Vibrator.vibrateCompat(timings: LongArray, repeat: Int = -1) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate(VibrationEffect.createWaveform(timings, repeat))
    } else {
        @Suppress("DEPRECATION")
        vibrate(timings, repeat)
    }
}