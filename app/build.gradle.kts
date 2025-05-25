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
        kotlinCompilerExtensionVersion = "1.5.10" // Keep this as it is tied to your Kotlin version
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

    // Material icons
    implementation("androidx.compose.material:material-icons-extended:1.6.7")

    // Base Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")

    // Jetpack Compose BOM pour gérer les versions de Compose
    // C'est la meilleure pratique pour éviter les incohérences de versions de Compose
    implementation(platform("androidx.compose:compose-bom:2024.05.00")) // Updated to the latest stable version
    implementation("androidx.activity:activity-compose:1.9.0") // Updated for consistency with BOM
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3") // Using Material3
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7") // Updated for consistency and compatibility

    // Architecture MVVM (Lifecycle ViewModel)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // Updated to the latest stable version
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0") // Updated
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // Added for Compose Hilt integration

    // Hilt (version et compilateurs)
    val hiltVersion = "2.51" // Use the latest stable version of Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")

    // Hilt AndroidX Integration (Ceci est la clé pour votre problème)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // For injecting Hilt ViewModels in Compose navigation
    kapt("androidx.hilt:hilt-compiler:1.2.0") // IMPÉRATIF for AndroidX components using Hilt

    // Room
    val roomVersion = "2.6.1" // Use the latest stable version of Room
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Kotlin Extensions and Coroutines support for Room

    // Retrofit (pour les requêtes réseau) - if you actually use it
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coroutines (if you don't already have them via other libs, e.g., Room-ktx)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // Updated to the latest stable version

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    // For Hilt-injectable workers (if DailyResetWorker uses Hilt)
    implementation("androidx.hilt:hilt-work:1.2.0") // Updated for consistency with hilt-compiler
    kapt("androidx.hilt:hilt-compiler:1.2.0") // Ensure this matches hilt-work version

    // Accompanist Swipe Refresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0") // Kept your updated version

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.7")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7") // Updated for consistency with Compose BOM
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.7") // Add this for Compose UI tests
}
