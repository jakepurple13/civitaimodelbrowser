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
    jvm {
        compilations.all {
            kotlin {
                compilerOptions {
                    jvmToolchain(17)
                }
            }
        }
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":common"))
            implementation(compose.desktop.currentOs)
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

compose.desktop {
    application {
        mainClass = "MainKt"
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
                .file("src/jvmMain/resources/")
                .resolve("civitai_logo.$extension")
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/civitmaclogo.icns"))
                dockName = "CivitAi Model Browser"
            }
            windows {
                iconFile.set(iconFile("ico"))
                dirChooser = true
                console = true
            }
            linux {
                iconFile.set(iconFile("png"))
            }
        }
    }
}
