package com.example.stopprogressif // ← le package doit être exactement celui‑ci !

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Point d’entrée de l’application – initialise Hilt. */
@HiltAndroidApp
class MyApplication : Application()