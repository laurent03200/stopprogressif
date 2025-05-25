package com.stopprogressif

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/* -------------------------------------------------------------------------- */
/* Extension DataStore – attachée au context d’application pour éviter les   */
/* fuites mémoire quand une Activity est détruite.                            */
/* -------------------------------------------------------------------------- */
private val Context.userPrefsDataStore by preferencesDataStore(name = "user_preferences")

