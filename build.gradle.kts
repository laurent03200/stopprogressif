// Fichier build.gradle.kts placé à la racine (même dossier que settings.gradle.kts)

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Gradle Android Plugin & Kotlin Gradle Plugin
        classpath("com.android.tools.build:gradle:8.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")

        // ➜ Plugin Hilt (la version doit être la même que dans :app)
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
    }
}

// Tâche utilitaire « gradlew clean »
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}



