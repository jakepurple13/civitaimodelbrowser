import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.compose")
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.easylauncher)
}

if (file("google-services.json").exists()) {
    println("Applying google services")
    apply(plugin = libs.plugins.google.gms.google.services.get().pluginId)
    apply(plugin = libs.plugins.google.firebase.crashlytics.get().pluginId)
    apply(plugin = libs.plugins.google.firebase.performance.get().pluginId)
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

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
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        create("beta") {
            initWith(getByName("debug"))
            matchingFallbacks.add("debug")
            isDebuggable = false
        }

        create("betaMinified") {
            initWith(getByName("debug"))
            matchingFallbacks.add("debug")
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("release") {
            isMinifyEnabled = false
        }

        create("releaseMinified") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xexplicit-backing-fields")
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