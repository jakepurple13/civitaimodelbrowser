import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.version.get()
    id("com.codingfeline.buildkonfig")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.aboutLibraries)
    kotlin("native.cocoapods")
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    android {
        compileSdk = 36
        minSdk = 28
        namespace = "com.programmersbox.common"
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        androidResources {
            enable = true
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "common"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.ui)
            api(compose.foundation)
            api(compose.materialIconsExtended)
            api(libs.material3.window.size)
            //api(compose.material3)
            api(libs.material3)
            api(compose.components.resources)
            api(libs.ktor.core)
            api(libs.ktor.content.negotiation)
            api(libs.ktor.serialization)
            api(libs.datastore.core)
            api(libs.datastore.core.okio)
            api(libs.datastore.preferences)
            api(libs.paging.runtime)
            api(libs.paging.compose)
            //api(libs.paging.compose)
            api(libs.kamel.image)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.coroutines.core)
            api(libs.lifecycle.viewmodel.compose)
            api(libs.lifecycle.runtime.compose)
            //api(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.ksoup)
            api(libs.haze)
            api(libs.haze.materials)
            api(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            //add("ksp", libs.androidx.room.compiler)

            api(libs.cmp.navigation3.ui)
            api(libs.cmp.lifecycle.viewmodel.navigation3)
            api(libs.cmp.navigationevent.compose)
            implementation(libs.cmp.material3.adaptive.nav3)
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.koin.compose)
            implementation(libs.koin.viewmodel)
            implementation(libs.koin.nav3)
            api(libs.qrose)
            api(libs.filekit.core)
            api(libs.filekit.dialogs)
            api(libs.filekit.dialogs.compose)

            implementation(libs.blurhash)
            implementation(libs.compose.multiplatform.media.player)

            api(libs.material.kolor)

            implementation(libs.connectivity.core)
            implementation(libs.connectivity.compose)

            implementation(libs.kotlinx.collections.immutable)

            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.core)
            implementation(libs.aboutlibraries.compose.m3)

            implementation(libs.okio)

            implementation(libs.composewebview)

            implementation(libs.multiplatform.markdown.renderer)
            implementation(libs.multiplatform.markdown.renderer.m3)

            implementation(libs.chroma.dial)

            implementation(libs.backdrop)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            api(libs.androidx.appcompat)
            api(libs.androidx.core)
            api(libs.ktor.jvm)
            api(libs.koin.android)
            api(libs.kamel.image.bitmap.resizing)
            api(libs.barcode.scanning)
            api(libs.coroutinesPlayServices)
            implementation(libs.connectivity.device)
            implementation(libs.connectivity.compose.device)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.biometric.compose)
            api(libs.androidx.work.runtime.ktx)
            api(libs.koin.workmanager)

            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.perf)
        }

        jvmMain.dependencies {
            api(compose.preview)
            api(libs.ktor.jvm)
            api(libs.kotlinx.coroutines.swing)
            api(libs.kotlin.multiplatform.appdirs)
            api(libs.zxing.javase)
            api(libs.zxing.core)
            implementation(libs.connectivity.http)
            implementation(libs.connectivity.compose.http)

            implementation(libs.javacv)
            implementation(libs.ffmpeg.platform)

            implementation(libs.camerak)
            implementation(libs.kashif.mehmood.km.qr.scanner.plugin)
        }

        iosMain.dependencies {
            implementation(libs.connectivity.device)
            implementation(libs.connectivity.compose.device)
            implementation(libs.biometricauth)
            implementation(libs.kmp.io)

            implementation(libs.gitlive.firebase.analytics)
            implementation(libs.gitlive.firebase.crashlytics)
            implementation(libs.gitlive.firebase.perf)
            implementation(libs.kotlin.native.zxing)

            implementation(libs.permissions)
            implementation(libs.permissions.notifications)
            implementation(libs.alarmee)
        }

        val deviceMain by creating {
            dependsOn(commonMain.get())
            androidMain.get().dependsOn(this)
            iosMain.get().dependsOn(this)

            dependencies {
                api(libs.scanner)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.programmersbox.resources"
    generateResClass = always
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

buildkonfig {
    packageName = "com.programmersbox.common"
    defaultConfigs {
        buildConfigField(
            type = STRING,
            name = "VERSION_NAME",
            value = libs.versions.appVersion.get(),
            const = true
        )
    }
}

aboutLibraries {
    export {
        // Define the output path for manual generation
        // Adjust the path based on your project structure (e.g., composeResources, Android res/raw)
        outputPath =
            file("${project.projectDir.path}/src/commonMain/composeResources/files/aboutlibraries.json")
        // Optionally specify the variant for export
        // variant = "release"
    }
}