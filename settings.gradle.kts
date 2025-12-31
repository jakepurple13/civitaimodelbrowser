pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

buildscript {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
        maven("https://jogamp.org/deployment/maven/")
    }
}

rootProject.name = "civitaimodelbrowser"

include(":android")
include(":desktop")
include(":common")