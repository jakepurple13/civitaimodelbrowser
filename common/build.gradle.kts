import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.internal.utils.localPropertiesFile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.version.get()
    id("com.codingfeline.buildkonfig")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.ui)
            api(compose.foundation)
            api(compose.materialIconsExtended)
            //api(compose.material3)
            api("org.jetbrains.compose.material3:material3:1.10.0-alpha05")
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
            api(libs.lifecycle.viewmodel.compose)
            api(libs.lifecycle.runtime.compose)
            //api(libs.androidx.lifecycle.runtime.compose)
            api(libs.jsoup)
            api(libs.haze)
            api(libs.haze.materials)
            api(libs.sonner)
            api(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            //add("ksp", libs.androidx.room.compiler)

            implementation(libs.cmp.navigation3.ui)
            implementation(libs.cmp.lifecycle.viewmodel.navigation3)
            implementation(libs.cmp.navigationevent.compose)
            implementation(libs.cmp.material3.adaptive.nav3)
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.koin.compose)
            implementation(libs.koin.viewmodel)
            implementation(libs.koin.nav3)
            api(libs.qrose)
            api(libs.scanner)
            api(libs.filekit.core)
            api(libs.filekit.dialogs)
            api(libs.filekit.dialogs.compose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            api(libs.androidx.appcompat)
            api(libs.androidx.core)
            api(libs.ktor.jvm)
            implementation(libs.composeScrollbars)
            api(libs.koin.android)
            api(libs.kamel.image.bitmap.resizing)
            api(libs.barcode.scanning)
            api(libs.coroutinesPlayServices)
        }

        jvmMain.dependencies {
            api(compose.preview)
            api(libs.ktor.jvm)
            api(libs.kotlinx.coroutines.swing)
            api("ca.gosyer:kotlin-multiplatform-appdirs:2.0.0")
            api(libs.zxing.javase)
            api(libs.zxing.core)
        }

        //val desktopTest by getting

    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.programmersbox.resources"
    generateResClass = always
}

android {
    compileSdk = 36
    namespace = "com.programmersbox.common"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    defaultConfig {
        minSdk = 28
        targetSdk = 35
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

room {
    schemaDirectory("$projectDir/schemas")
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
    return if (localProperties.isFile) {
        InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
        properties.getProperty(key)
    } else if (System.getenv("CI") != null) {
        System.getProperty("API_KEY")
    } else error("File from not found")
}