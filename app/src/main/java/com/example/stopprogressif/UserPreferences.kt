package com.stopprogressif

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/* -------------------------------------------------------------------------- */
/* Extension DataStore – attachée au context d’application pour éviter les   */
/* fuites mémoire quand une Activity est détruite.                            */
/* -------------------------------------------------------------------------- */
private val Context.userPrefsDataStore by preferencesDataStore(name = "user_preferences")

/**
 * Gestion simplifiée des préférences utilisateur (prix du paquet, objectifs, etc.).
 *
 * — Expose des *Flow* réactifs pour observer les changements.
 * — Fournit des méthodes `save…` pour modifier chaque valeur.
 * — Toutes les clés sont centralisées dans `object Keys`.
 */
class UserPreferences(context: Context) {

    private val dataStore = context.applicationContext.userPrefsDataStore

    /* ---------------------------------------------------------------------- */
    /* Clés                                                                   */
    /* ---------------------------------------------------------------------- */
    private object Keys {
        val PRIX_PAQUET = floatPreferencesKey("prix_paquet")
        val CIG_PAR_JOUR = intPreferencesKey("cigarettes_par_jour")
        val OBJECTIF = intPreferencesKey("objectif_cigarettes")
    }

    /* ---------------------------------------------------------------------- */
    /* Flows de lecture                                                       */
    /* ---------------------------------------------------------------------- */
    val prixPaquetFlow: Flow<Float> = dataStore.data
        .map { it[Keys.PRIX_PAQUET] ?: 0f }

    val cigarettesParJourFlow: Flow<Int> = dataStore.data
        .map { it[Keys.CIG_PAR_JOUR] ?: 0 }

    val objectifCigarettesFlow: Flow<Int> = dataStore.data
        .map { it[Keys.OBJECTIF] ?: 0 }

    /* ---------------------------------------------------------------------- */
    /* Fonctions de sauvegarde                                                 */
    /* ---------------------------------------------------------------------- */
    suspend fun savePrixPaquet(value: Float) = dataStore.edit {
        it[Keys.PRIX_PAQUET] = value
    }

    suspend fun saveCigarettesParJour(value: Int) = dataStore.edit {
        it[Keys.CIG_PAR_JOUR] = value
    }

    suspend fun saveObjectifCigarettes(value: Int) = dataStore.edit {
        it[Keys.OBJECTIF] = value
    }
}
