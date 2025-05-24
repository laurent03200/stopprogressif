package com.example.stopprogressif.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.stopprogressif.model.DailyReport
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.trackerDataStore by preferencesDataStore(name = "tracker_settings")

class CigaretteTrackerStoreManager(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.trackerDataStore

    private object Keys {
        val LAST_CIG_TIME = longPreferencesKey("lastCigaretteTime")
        val CIG_COUNT = longPreferencesKey("cigarettesFumees")
    }

    suspend fun getLastCigaretteTime(): Long? {
        return dataStore.data
            .catch { Log.e("TrackerStore", "Erreur", it); emit(emptyPreferences()) }
            .map { prefs -> prefs[Keys.LAST_CIG_TIME] }
            .first()
    }

    suspend fun loadAllDailyReports(): List<DailyReport> {
        // À adapter selon ton vrai système de sauvegarde JSON si besoin
        return emptyList()
    }

    suspend fun loadStateWithTimestamp(): Triple<Long, Int, Long> {
        val prefs = dataStore.data.first()
        val savedTime = 60 * 60 * 1000L  // 1h par défaut
        val count = prefs[Keys.CIG_COUNT]?.toInt() ?: 0
        val lastUpdate = prefs[Keys.LAST_CIG_TIME] ?: System.currentTimeMillis()
        return Triple(savedTime, count, lastUpdate)
    }

    suspend fun saveLastCigaretteTime(time: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_CIG_TIME] = time
        }
    }

    suspend fun saveCigarettesFumees(count: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.CIG_COUNT] = count.toLong()
        }
    }
}
