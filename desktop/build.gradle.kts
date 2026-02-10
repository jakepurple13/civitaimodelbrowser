import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.compose.compiler)
    id("com.codingfeline.buildkonfig")
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
    jvm {
        compilations.all {
            kotlin {
                compilerOptions {
                    jvmToolchain(24)
                    freeCompilerArgs.add("-Xexplicit-backing-fields")
                }
            }
        }
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":common"))
            implementation(compose.desktop.currentOs)

            if (System.getenv("CI") != null)
                implementation(libs.ktor.monitor.logging.no.op)
            else
                implementation(libs.ktor.monitor.logging)
        }
        //val jvmTest by getting
    }
}

buildkonfig {
    packageName = "com.programmersbox.desktop"
    defaultConfigs {
        buildConfigField(
            type = STRING,
            name = "VERSION_NAME",
            value = libs.versions.appVersion.get(),
            const = true
        )
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.programmersbox.desktop.resources"
    generateResClass = always
    nameOfResClass = "DesktopResources"
    customDirectory(
        sourceSetName = "jvmMain",
        directoryProvider = provider { layout.projectDirectory.dir("src/jvmMain/resources") }
    )
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += "--enable-native-access=ALL-UNNAMED"
        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Exe, TargetFormat.Msi,
                TargetFormat.Deb
            )
            packageName = "CivitAi Model Browser"
            packageVersion = libs.versions.appVersion.get()
            vendor = "jakepurple13"

            modules("jdk.unsupported")
            modules("jdk.unsupported.desktop")

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            fun iconFile(extension: String) = project
                .file("src/jvmMain/resources/files/")
                .resolve("civitai_logo.$extension")
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/files/civitmaclogo.icns"))
                dockName = "CivitAi Model Browser"
            }
            windows {
                iconFile.set(iconFile("ico"))
                dirChooser = true
                console = true
                menu = true
                menuGroup = "CivitAi Model Browser"
            }
            linux {
                iconFile.set(iconFile("png"))
            }
        }
    }
}
