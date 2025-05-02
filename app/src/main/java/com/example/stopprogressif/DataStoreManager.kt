package com.stopprogressif

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/* Extension DataStore liée au contexte d’application */
private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class DataStoreManager(context: Context) {

    private val dataStore: DataStore<Preferences> =
        context.applicationContext.settingsDataStore

    /* ---------------- Clés ---------------- */
    private object Keys {
        val PRIX            = floatPreferencesKey("prixPaquet")
        val PAR_PAQUET      = intPreferencesKey("cigarettesParPaquet")
        val MODE            = stringPreferencesKey("mode")
        val OBJECTIF        = intPreferencesKey("objectifParJour")

        /* Plage horaire active                       */
        val HEURE_DEBUT     = intPreferencesKey("heuresDebut")
        val MIN_DEBUT       = intPreferencesKey("minutesDebut")
        val HEURE_FIN       = intPreferencesKey("heuresFin")
        val MIN_FIN         = intPreferencesKey("minutesFin")

        /* Intervalle fixe                            */
        val HEURES          = intPreferencesKey("heuresEntreCigarettes")
        val MINUTES         = intPreferencesKey("minutesEntreCigarettes")

        val HABITUELLES     = intPreferencesKey("cigarettesHabituelles")

        /* État courant                               */
        val TEMPS_RESTANT   = longPreferencesKey("tempsRestant")
        val CIG_FUMEES      = intPreferencesKey("cigarettesFumees")
        val LAST_UPDATE     = longPreferencesKey("lastUpdateTime")
    }

    /* ---------------- Lecture réactive ---------------- */
    val settingsFlow: Flow<SettingsData> = dataStore.data.map { p ->
        SettingsData(
            prixPaquet              = p[Keys.PRIX] ?: 10f,
            cigarettesParPaquet     = p[Keys.PAR_PAQUET] ?: 20,
            mode                    = p[Keys.MODE] ?: SettingsData.MODE_OBJECTIF,
            objectifParJour         = p[Keys.OBJECTIF] ?: 20,

            heuresDebut             = p[Keys.HEURE_DEBUT] ?: 7,
            minutesDebut            = p[Keys.MIN_DEBUT] ?: 0,
            heuresFin               = p[Keys.HEURE_FIN] ?: 23,
            minutesFin              = p[Keys.MIN_FIN] ?: 0,

            heuresEntreCigarettes   = p[Keys.HEURES] ?: 1,
            minutesEntreCigarettes  = p[Keys.MINUTES] ?: 0,
            cigarettesHabituelles   = p[Keys.HABITUELLES] ?: 30
        )
    }

    /* ---------------- Écriture paramètres ---------------- */
    suspend fun saveSettings(s: SettingsData) {
        dataStore.edit { p ->
            p[Keys.PRIX]             = s.prixPaquet
            p[Keys.PAR_PAQUET]       = s.cigarettesParPaquet
            p[Keys.MODE]             = s.mode
            p[Keys.OBJECTIF]         = s.objectifParJour

            p[Keys.HEURE_DEBUT]      = s.heuresDebut
            p[Keys.MIN_DEBUT]        = s.minutesDebut
            p[Keys.HEURE_FIN]        = s.heuresFin
            p[Keys.MIN_FIN]          = s.minutesFin

            p[Keys.HEURES]           = s.heuresEntreCigarettes
            p[Keys.MINUTES]          = s.minutesEntreCigarettes
            p[Keys.HABITUELLES]      = s.cigarettesHabituelles
        }
    }

    /* ---------------- État courant (timer) -------------- */
    suspend fun saveStateWithTimestamp(tempsRestant: Long, fumees: Int) {
        dataStore.edit { p ->
            p[Keys.TEMPS_RESTANT] = tempsRestant
            p[Keys.CIG_FUMEES]    = fumees
            p[Keys.LAST_UPDATE]   = System.currentTimeMillis()
        }
    }

    /* ---------------- Accès synchrones (init) ------------ */
    fun loadSettings(): SettingsData = runBlocking { settingsFlow.first() }

    fun loadStateWithTimestamp(): Triple<Long, Int, Long> = runBlocking {
        val p = dataStore.data.first()
        Triple(
            p[Keys.TEMPS_RESTANT] ?: -1L,
            p[Keys.CIG_FUMEES]    ?: 0,
            p[Keys.LAST_UPDATE]   ?: System.currentTimeMillis()
        )
    }

    fun getLastUpdateTime(): Long = runBlocking {
        dataStore.data.first()[Keys.LAST_UPDATE] ?: System.currentTimeMillis()
    }
}
