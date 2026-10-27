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
        // Used in CI. ATD images can't collect profiles, so use the full AOSP image
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

val isCi = System.getenv("CI") != null

baselineProfile {
    if (isCi) managedDevices += "pixel6Api34"
    useConnectedDevices = !isCi
}

dependencies {
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

// Generate profiles:  ./gradlew :android:generateReleaseBaselineProfile
// Run benchmarks:     ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest
