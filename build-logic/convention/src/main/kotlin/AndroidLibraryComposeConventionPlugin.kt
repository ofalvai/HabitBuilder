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

@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import shared.buildComposeMetricsParameters

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            // Configure android { ... } block
            extensions.configure<LibraryExtension> {
                buildFeatures.compose = true

                composeOptions.kotlinCompilerExtensionVersion = libs.findVersion("compose-compiler").get().toString()

                // kotlinOptions { ... }
                (this as ExtensionAware).extensions.configure<KotlinJvmOptions>("kotlinOptions") {
                    freeCompilerArgs = freeCompilerArgs + buildComposeMetricsParameters()
                }
            }

            dependencies {
                add("implementation", platform(libs.findLibrary("compose.bom").get()))
                add("api", libs.findLibrary("compose.material3").get())
                add("implementation", libs.findLibrary("compose.ui.ui").get())
                add("implementation", libs.findLibrary("compose.animation").get())
                add("implementation", libs.findLibrary("compose.ui.toolingpreview").get())
                add("debugImplementation", libs.findLibrary("compose.ui.tooling").get())
                add("implementation", libs.findLibrary("showkase.annotation").get())
            }
        }
    }

}

