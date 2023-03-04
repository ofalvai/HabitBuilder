/*
 * Copyright 2023 Olivér Falvai
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

package shared

import Constants
import com.android.build.api.dsl.CompileOptions
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

fun Project.setJvmToolchain() {
    extensions.configure<KotlinProjectExtension> {
        jvmToolchain {
            languageVersion.set(Constants.javaToolchainVersion)
        }
    }
}

fun KotlinJvmOptions.setAndroidJvmTarget() {
    jvmTarget = Constants.javaToolchainVersion.toString()
}

fun CompileOptions.setJvmTargets() {
    sourceCompatibility = JavaVersion.toVersion(Constants.javaToolchainVersion)
    targetCompatibility = JavaVersion.toVersion(Constants.javaToolchainVersion)
    isCoreLibraryDesugaringEnabled = true
}