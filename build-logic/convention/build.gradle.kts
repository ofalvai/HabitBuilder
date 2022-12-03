plugins {
    `kotlin-dsl`
}

group = "com.ofalvai.habittracker.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.gradle.android.plugin)
    implementation(libs.kotlin.gradle)
}

gradlePlugin {
    plugins {
        register("androidApp") {
            id = "habittracker.android.app"
            implementationClass = "AndroidAppConventionPlugin"
        }
        register("androidLibrary") {
            id = "habittracker.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "habittracker.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
    }
}