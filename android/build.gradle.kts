import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.compose")
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.easylauncher)
    alias(libs.plugins.hotswan.compiler)
    alias(libs.plugins.androidx.baselineprofile)
}

if (file("google-services.json").exists()) {
    println("Applying google services")
    apply(plugin = libs.plugins.google.gms.google.services.get().pluginId)
    apply(plugin = libs.plugins.google.firebase.crashlytics.get().pluginId)
    apply(plugin = libs.plugins.google.firebase.performance.get().pluginId)
}

baselineProfile {
    // Keep generated profiles in src/<variant>/generated/baselineProfiles so they are committed
    saveInSrc = true
    // Regenerate manually with ./gradlew :android:generateReleaseBaselineProfile
    automaticGenerationDuringBuild = false
    // Also collects startup-prof.txt for R8 startup DEX layout optimization
    dexLayoutOptimization = true
}

// Profiles are generated on x86_64 emulators in CI, so pass -PincludeEmulatorAbis there
val releaseAbis = buildList {
    add("arm64-v8a")
    if (providers.gradleProperty("includeEmulatorAbis").isPresent) add("x86_64")
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

android {
    namespace = "com.programmersbox.civitaimodelbrowser"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.programmersbox.civitaimodelbrowser"
        minSdk = 28
        targetSdk = 37
        versionCode = 16
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
            versionNameSuffix = "-debug"
        }

        create("beta") {
            initWith(getByName("debug"))
            matchingFallbacks.add("debug")
            isDebuggable = false
            versionNameSuffix = "-beta"
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
            versionNameSuffix = "-betaMinified"
        }

        getByName("release") {
            isMinifyEnabled = true
            // Only ship 64-bit ARM native libs; x86/x86_64 are emulator-only and 32-bit adds ~4 MB
            ndk { abiFilters += releaseAbis }
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("releaseMinified") {
            isMinifyEnabled = true
            // Only ship 64-bit ARM native libs; x86/x86_64 are emulator-only and 32-bit adds ~4 MB
            ndk { abiFilters += releaseAbis }
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    sourceSets {
        // releaseMinified matches release, so reuse the profiles generated for release
        getByName("releaseMinified").baselineProfiles.directories.add("src/release/generated/baselineProfiles")
    }
}

androidComponents {
    // Release has no signing config (CI signs it), so sign the baseline profile / benchmark
    // variants with the debug key so they can be installed on a device
    onVariants(selector().withBuildType("nonMinifiedRelease")) {
        it.signingConfig.from(android.signingConfigs.getByName("debug"))
    }
    onVariants(selector().withBuildType("benchmarkRelease")) {
        it.signingConfig.from(android.signingConfigs.getByName("debug"))
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
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    debugImplementation(libs.debugoverlay)
}