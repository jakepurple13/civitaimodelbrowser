import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.internal.utils.localPropertiesFile
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.version.get()
    id("io.realm.kotlin") version libs.versions.realm.get()
    id("com.codingfeline.buildkonfig")
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
                api(libs.sonner)

                val lifecycle_version = "2.8.0-alpha04"
                api("androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version")
                api("androidx.lifecycle:lifecycle-common:$lifecycle_version")
                api("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
                //api("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-dev1561")
                api("androidx.lifecycle:lifecycle-runtime:$lifecycle_version")
                //implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version")

                implementation(libs.internal.lifecycle.viewmodel.compose)
                implementation(libs.internal.navigation.compose)
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
                implementation(libs.composeScrollbars)
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

buildkonfig {
    packageName = "com.programmersbox.common"
    defaultConfigs {
        val apiKey: String? = getLocalProperty("api_key") as? String

        buildConfigField(
            type = STRING,
            name = "API_KEY",
            value = apiKey ?: "",
            const = true
        )
    }
}

fun Project.getLocalProperty(key: String): Any? {
    val properties = Properties()
    val localProperties = localPropertiesFile
    if (localProperties.isFile) {
        InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    } else error("File from not found")

    return properties.getProperty(key)
}