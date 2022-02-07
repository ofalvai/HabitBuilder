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

package com.ofalvai.habittracker.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.ui.model.OnboardingState
import com.ofalvai.habittracker.ui.theme.PreviewTheme
import com.ofalvai.habittracker.ui.theme.PreviewTheme2
import com.ofalvai.habittracker.ui.theme.surfaceVariant
import kotlin.math.roundToInt

@Composable
fun Onboarding(state: OnboardingState) {
    Box(
        modifier = Modifier
            .animateContentSize(animationSpec = tween(500))
            .padding(16.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colors.surfaceVariant, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val progress = state.step.index / state.totalSteps.toFloat()
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )
            Box(Modifier.size(48.dp)) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.surface
                )
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.caption
                )
            }

            Column(Modifier.padding(start = 24.dp)) {
                Text(
                    text = stringResource(state.step.title),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(state.step.subtitle),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Preview
@ShowkaseComposable(name = "Dashboard item", group = "Onboarding", styleName = "Initial")
@Composable
fun PreviewOnboardingStep1() {
    PreviewTheme {
        val state = OnboardingState(
            step = OnboardingData.steps[0],
            totalSteps = OnboardingData.totalSteps
        )
        Onboarding(state)
    }
}

@Preview
@ShowkaseComposable(name = "Dashboard item", group = "Onboarding", styleName = "Step 2")
@Composable
fun PreviewOnboardingStep2() {
    PreviewTheme2 {
        val state = OnboardingState(
            step = OnboardingData.steps[1],
            totalSteps = OnboardingData.totalSteps
        )
        Onboarding(state)
    }
}