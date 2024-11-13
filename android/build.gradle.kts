plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

android {
    namespace = "com.programmersbox.civitaimodelbrowser"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.programmersbox.civitaimodelbrowser"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0-SNAPSHOT"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildTypes {
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
}

tasks.register("BuildAndRun") {
    doFirst {
        exec {
            workingDir(projectDir.parentFile)
            commandLine("./gradlew", "android:build")
            commandLine("./gradlew", "android:installDebug")
        }
    }
}