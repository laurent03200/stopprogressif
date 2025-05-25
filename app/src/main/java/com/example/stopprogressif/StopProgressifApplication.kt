package com.example.stopprogressif

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Ceci est la classe Application de votre application, essentielle pour Hilt.
// Elle doit être déclarée dans AndroidManifest.xml sous <application android:name=".StopProgressifApplication">
@HiltAndroidApp
class StopProgressifApplication : Application() {
    // Vous pouvez y ajouter des initialisations globales si nécessaire.
    // Pour l'instant, elle peut rester vide.
}