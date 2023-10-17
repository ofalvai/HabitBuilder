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

package com.ofalvai.habittracker.core.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

/**
 * Navigation transitions mirroring the Material spec and the non-Compose Material Design Components
 */
object AppTransition {
    val defaultEnter = fadeIn()
    val defaultExit = fadeOut()

    val fadeThroughEnter = fadeIn(
        initialAlpha = 0.35f,
        animationSpec = tween(durationMillis = 210, delayMillis = 90)
    ) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(durationMillis = 210, delayMillis = 90)
    )
    val fadeThroughExit = fadeOut(animationSpec = tween(durationMillis = 90))

    val sharedZAxisEnterForward = scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(durationMillis = 60, delayMillis = 60, easing = LinearEasing))
    val sharedZAxisEnterBackward = scaleIn(
        initialScale = 1.1f,
        animationSpec = tween(300)
    )
    val sharedZAxisExitForward = scaleOut(
        targetScale = 1.1f,
        animationSpec = tween(durationMillis = 300),
    )
    val sharedZAxisExitBackward = scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(durationMillis = 300),
    ) + fadeOut(animationSpec = tween(durationMillis = 60, delayMillis = 60, easing = LinearEasing))
}