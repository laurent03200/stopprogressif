plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin") // Plugin Hilt
    id("kotlin-kapt") // Plugin Kapt pour les processeurs d'annotations
}

android {
    namespace = "com.example.stopprogressif"
    compileSdk = 35 // La version la plus récente est toujours recommandée

    defaultConfig {
        applicationId = "com.example.stopprogressif"
        minSdk = 26
        targetSdk = 34 // Toujours target la dernière version stable
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Assurez-vous que cette version est compatible avec votre version de Compose BOM
        // et de Kotlin Gradle Plugin.
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    compileOptions {
        // Utilisez la même version JVM Target que votre jvmTarget pour Kotlin
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    // Jetpack DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Material icons (BarChart support, optionally replace with your own chart lib)
    implementation("androidx.compose.material:material-icons-extended:1.6.7") // Mis à jour vers une version plus récente pour cohérence

    // Optional: MPAndroidChart if used for charts
    // Assurez-vous d'avoir configuré le dépôt JitPack dans votre settings.gradle si vous utilisez cette lib
    // implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


    // Base Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")

    // Jetpack Compose BOM pour gérer les versions de Compose
    // C'est la meilleure pratique pour éviter les incohérences de versions de Compose
    implementation(platform("androidx.compose:compose-bom:2024.05.00")) // Mis à jour vers la version stable la plus récente
    implementation("androidx.activity:activity-compose:1.9.0") // Mis à jour pour être plus proche du BOM
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3") // Utilisation de Material3
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Note: Vous aviez 'androidx.compose.material:material:1.5.4', si vous utilisez Material3,
    // l'ancienne bibliothèque Material est souvent redondante sauf pour des cas spécifiques.

    // Navigation Compose (après avoir utilisé le BOM pour Compose, on peut préciser la version ici si besoin)
    implementation("androidx.navigation:navigation-compose:2.7.7") // Mis à jour pour cohérence et compatibilité

    // Architecture MVVM (Lifecycle ViewModel)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // Mis à jour vers la dernière version stable
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0") // Mis à jour

    // Hilt (version et compilateurs)
    val hiltVersion = "2.51" // Utilisez la dernière version stable de Hilt (2.51 est la plus récente au 21 mai 2025)
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")

    // Hilt AndroidX Integration (Ceci est la clé pour votre problème)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // Pour injecter les ViewModels Hilt dans la navigation Compose
    kapt("androidx.hilt:hilt-compiler:1.2.0") // IMPÉRATIF pour les composants AndroidX utilisant Hilt


    // Room
    val roomVersion = "2.6.1" // Utilisez la dernière version stable de Room
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Kotlin Extensions and Coroutines support for Room

    // Retrofit (pour les requêtes réseau)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coroutines (si vous ne les avez pas déjà via d'autres lib, ex: Room-ktx)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // Mis à jour vers la dernière version stable

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")


    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.7")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7") // Mis à jour pour cohérence avec Compose BOM
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.7") // Ajoutez ceci pour les tests UI Compose
}