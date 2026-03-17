import com.android.build.api.dsl.ManagedVirtualDevice
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.programmersbox.civitaimodelbrowser.benchmark"
    compileSdk = 36
    targetProjectPath = ":android"

    experimentalProperties["android.experimental.self-instrumenting"] = true

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] =
            "EMULATOR,LOW-BATTERY"
    }

    buildTypes {
        create("benchmark") {
            matchingFallbacks += listOf(
                "benchmark",
                "release",
                "beta",
                "releaseMinified",
                "betaMinified"
            )
        }

        create("beta") {
            matchingFallbacks += listOf(
                "beta",
                "benchmark",
                "release",
                "releaseMinified",
                "betaMinified"
            )
        }

        create("betaMinified") {
            matchingFallbacks += listOf(
                "betaMinified",
                "beta",
                "benchmark",
                "releaseMinified",
                "release"
            )
        }

        create("releaseMinified") {
            matchingFallbacks += listOf("releaseMinified", "release", "benchmark")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    if(System.getenv("CI") == null) {
        testOptions.managedDevices.allDevices {
            create<ManagedVirtualDevice>("pixel6Api35") {
                device = "Pixel 6"
                apiLevel = 35
                systemImageSource = "google"
            }
        }
    }

}

baselineProfile {
    if(System.getenv("CI") == null) managedDevices += "pixel6Api35"
    useConnectedDevices = System.getenv("CI") == null
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation("androidx.test.ext:junit:1.3.0")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
}

// Example runs:
// ./gradlew :benchmark:assembleBenchmarkBenchmark
// ./gradlew :benchmark:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.programmersbox.civitaimodelbrowser.benchmark.StartupBenchmark