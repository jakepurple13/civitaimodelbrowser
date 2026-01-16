pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

buildscript {
    repositories {
        mavenCentral()
        maven { url = uri("https://storage.googleapis.com/r8-releases/raw") }
        maven("https://jogamp.org/deployment/maven/")
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
    }
}

rootProject.name = "civitaimodelbrowser"

include(":android")
include(":desktop")
include(":common")