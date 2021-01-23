package com.ofalvai.habittracker.ui.dashboard.view

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Interaction
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.gesture.pressIndicatorGestureFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ofalvai.habittracker.ui.HabitTrackerTheme
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
            Icon(Icons.Filled.Add, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Create new habit")
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
            DayLabel(day = mostRecentDay.minusDays(it.toLong()), isHighlighted = it == 0)
        }
    }
}

@Composable
fun DayLabel(
    day: LocalDate,
    isHighlighted: Boolean
) {
    val modifier = Modifier
        .wrapContentHeight(Alignment.Top)
        .padding(vertical = 8.dp)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = day.dayOfMonth.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption.copy(fontSize = 14.sp)
        )
        Text(
            text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold)
        )
        if (isHighlighted) {
            Surface(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(4.dp)
                    .align(Alignment.CenterHorizontally),
                shape = CircleShape,
                color = MaterialTheme.colors.primary
            ) {}
        }
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

fun Modifier.satisfyingToggleable(
    vibrator: Vibrator,
    rippleRadius: Dp,
    rippleBounded: Boolean,
    toggled: Boolean,
    onToggle: (Boolean) -> Unit,
    onSinglePress: () -> Unit
): Modifier {
    return composed {
        val interactionState = remember { InteractionState() }
        var isSinglePress by remember { mutableStateOf(false) }

        this
            .pressIndicatorGestureFilter(
                onStart = {
                    isSinglePress = true
                    vibrator.vibrateCompat(longArrayOf(0, 50))
                    interactionState.addInteraction(Interaction.Pressed, it)
                },
                onStop = {
                    if (isSinglePress) {
                        onSinglePress()
                    }
                    isSinglePress = false
                    interactionState.removeInteraction(Interaction.Pressed)
                },
                onCancel = {
                    isSinglePress = false
                    interactionState.removeInteraction(Interaction.Pressed)
                }
            )

            .longPressGestureFilter {
                isSinglePress = false
                vibrator.vibrateCompat(longArrayOf(0, 75, 50, 75))
                onToggle(!toggled)
            }
            .indication(interactionState, rememberRipple(radius = rippleRadius, bounded = rippleBounded))
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