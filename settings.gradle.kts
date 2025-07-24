pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.23"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = System.getenv("ProductName") ?: "Suwayomi-Server"

include("server")
include("server:i18n")
include("AndroidCompat")
include("AndroidCompat:Config")


