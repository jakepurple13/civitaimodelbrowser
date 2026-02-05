group = "com.programmersbox"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version libs.versions.kotlin.version.get() apply false
    kotlin("multiplatform") version libs.versions.kotlin.version.get() apply false
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.compose") version libs.versions.compose.version.get() apply false
    alias(libs.plugins.compose.compiler) apply false
    id("com.codingfeline.buildkonfig") version "0.17.1" apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.easylauncher) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
    alias(libs.plugins.google.firebase.performance) apply false
}