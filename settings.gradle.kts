pluginManagement {
    repositories {
        google()            // Plugins Android & Hilt
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // ✅ Ajout pour MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "StopProgressif"
include(":app")
