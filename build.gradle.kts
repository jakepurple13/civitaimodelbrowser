group = "com.programmersbox"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
        maven("https://jogamp.org/deployment/maven")
    }
}

plugins {
    kotlin("jvm") version libs.versions.kotlin.version.get() apply false
    kotlin("multiplatform") version libs.versions.kotlin.version.get() apply false
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.compose") version libs.versions.compose.version.get() apply false
    alias(libs.plugins.compose.compiler) apply false
    id("com.codingfeline.buildkonfig") version "0.17.1" apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.easylauncher) apply false
}