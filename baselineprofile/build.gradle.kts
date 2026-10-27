import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.programmersbox.civitaimodelbrowser.baselineprofile"
    compileSdk = 37
    targetProjectPath = ":android"

    defaultConfig {
        minSdk = 28
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    testOptions.managedDevices.allDevices {
        // ATD images can't collect profiles, so use the full AOSP image
        create<ManagedVirtualDevice>("pixel6Api34") {
            device = "Pixel 6"
            apiLevel = 34
            systemImageSource = "aosp"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Pass -PuseManagedDevice to generate on the pixel6Api34 Gradle Managed Device instead of a
// connected device or running emulator
val useManagedDevice = providers.gradleProperty("useManagedDevice").isPresent

baselineProfile {
    if (useManagedDevice) managedDevices += "pixel6Api34"
    useConnectedDevices = !useManagedDevice
}

dependencies {
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

// Generate profiles:  ./gradlew :android:generateReleaseBaselineProfile
// Run benchmarks:     ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest
