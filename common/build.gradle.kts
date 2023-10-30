plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.version.get()
    id("io.realm.kotlin") version libs.versions.realm.get()
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.ui)
                api(compose.foundation)
                api(compose.materialIconsExtended)
                api(compose.material3)
                api(compose.components.resources)
                api(libs.ktor.core)
                api(libs.ktor.content.negotiation)
                api(libs.ktor.serialization)
                api(libs.datastore.core)
                api(libs.datastore.core.okio)
                api(libs.datastore.preferences)
                api(libs.paging.runtime)
                //api(libs.paging.compose)
                api(libs.kamel.image)
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.coroutines.core)
                api(libs.precompose)
                api(libs.precompose.viewmodel)
                api(libs.jsoup)
                api(libs.realm.base)
                api(libs.haze)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.androidx.appcompat)
                api(libs.androidx.core)
                api(libs.ktor.jvm)
            }
        }

        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                api(libs.ktor.jvm)
            }
        }

        val desktopTest by getting

    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
