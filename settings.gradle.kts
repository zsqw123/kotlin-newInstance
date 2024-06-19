pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "newInstance"
includeBuild("insidePlugin")
include("kotlin-newInstance-gradle")
include(":kotlin-newInstance-kcp")

include(":kotlin-newInstance-runtime")
include(":demo")
