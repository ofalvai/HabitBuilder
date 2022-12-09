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

package com.ofalvai.habittracker.feature.dashboard.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.ofalvai.habittracker.core.common.OnboardingData
import com.ofalvai.habittracker.core.common.OnboardingState
import com.ofalvai.habittracker.core.ui.theme.PreviewTheme
import kotlin.math.roundToInt

@Composable
fun Onboarding(state: OnboardingState) {
    Box(
        modifier = Modifier
            .animateContentSize(animationSpec = tween(500))
            .padding(16.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
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
                    color = MaterialTheme.colorScheme.surface
                )
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(Modifier.padding(start = 24.dp)) {
                Text(
                    text = stringResource(state.step.title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(state.step.subtitle),
                    style = MaterialTheme.typography.bodyMedium
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
    PreviewTheme {
        val state = OnboardingState(
            step = OnboardingData.steps[1],
            totalSteps = OnboardingData.totalSteps
        )
        Onboarding(state)
    }
}