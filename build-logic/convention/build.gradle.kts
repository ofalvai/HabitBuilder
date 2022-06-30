plugins {
    `kotlin-dsl`
}

group = "com.ofalvai.habittracker.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(libs.android.tools.gradle)
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