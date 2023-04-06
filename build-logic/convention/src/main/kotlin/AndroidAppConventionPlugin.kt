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

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import shared.buildComposeMetricsParameters
import shared.setAndroidJvmTarget
import shared.setJvmTargets
import shared.setJvmToolchain

class AndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            // Configure android { ... } block
            extensions.configure<AppExtension> {
                compileSdkVersion = "android-${Constants.COMPILE_SDK}"

                defaultConfig {
                    minSdk = Constants.MIN_SDK
                    targetSdk = Constants.TARGET_SDK

                    versionCode = Constants.VERSION_CODE
                    versionName = Constants.VERSION_NAME
                }

                compileOptions {
                    setJvmTargets()
                }

                // kotlinOptions { ... }
                (this as ExtensionAware).extensions.configure<KotlinJvmOptions>("kotlinOptions") {
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    ) + buildComposeMetricsParameters()

                    setAndroidJvmTarget()
                }
            }

            setJvmToolchain()
        }
    }
}