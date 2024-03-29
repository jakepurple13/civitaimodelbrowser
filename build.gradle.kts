group = "com.programmersbox"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }
}

plugins {
    kotlin("jvm") version libs.versions.kotlin.version.get() apply false
    kotlin("multiplatform") version libs.versions.kotlin.version.get() apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("org.jetbrains.compose") version libs.versions.compose.version.get() apply false
}