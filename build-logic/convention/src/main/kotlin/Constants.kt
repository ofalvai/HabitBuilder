import org.gradle.api.JavaVersion

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

object Constants {
    const val MIN_SDK = 21
    const val COMPILE_SDK = 31
    const val TARGET_SDK = 31
    val JVM_TARGET = JavaVersion.VERSION_1_8

    const val VERSION_CODE = 1
    const val VERSION_NAME = "0.1.0"
}
