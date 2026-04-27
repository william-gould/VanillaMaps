pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "VanillaMaps"

include(":common")
include(":paper")
include(":fabric-1.21.11")

project(":common").projectDir = file("common")
project(":paper").projectDir = file("paper")