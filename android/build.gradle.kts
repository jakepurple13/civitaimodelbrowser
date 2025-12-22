import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.easylauncher)
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

android {
    namespace = "com.programmersbox.civitaimodelbrowser"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.programmersbox.civitaimodelbrowser"
        minSdk = 28
        targetSdk = 36
        versionCode = 15
        versionName = libs.versions.appVersion.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xexplicit-backing-fields")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = false
        }

        create("beta") {
            initWith(getByName("debug"))
            matchingFallbacks.add("debug")
            isDebuggable = false
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.material)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
}