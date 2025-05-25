package com.example.stopprogressif.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.stopprogressif.model.DailyReport
import kotlinx.coroutines.flow.first
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class DataStoreManager(private val context: Context) {

    object Keys {
        val DUREES_TENUES = stringPreferencesKey("durees_tenues")
        val SETTINGS = stringPreferencesKey("settings")
        val LAST_CIG_TIME = longPreferencesKey("last_cigarette_time")
        val NEXT_CIG_TIME = longPreferencesKey("next_cigarette_time")
        val STATE_INTERVAL = longPreferencesKey("state_interval")
        val STATE_COUNT = longPreferencesKey("state_count")
        val STATE_TIMESTAMP = longPreferencesKey("state_timestamp") // Ajouté
        val DAILY_REPORTS = stringPreferencesKey("daily_reports")
    }

    suspend fun saveSettings(settings: Settings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SETTINGS] = settings.serialize()
        }
    }

    suspend fun loadSettings(): Settings {
        val prefs = context.dataStore.data.first()
        val raw = prefs[Keys.SETTINGS]
        return if (raw != null) deserializeSettings(raw) else Settings()
    }

    // Modifié pour inclure le timestamp
    suspend fun saveStateWithTimestamp(interval: Long, count: Long, timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STATE_INTERVAL] = interval
            prefs[Keys.STATE_COUNT] = count
            prefs[Keys.STATE_TIMESTAMP] = timestamp // Sauvegarde du timestamp
            Log.d("DataStoreManager", "State saved: interval=$interval, count=$count, timestamp=$timestamp")
        }
    }

    // Modifié pour charger le timestamp
    suspend fun loadStateWithTimestamp(): Triple<Long, Long, Long?> {
        val prefs = context.dataStore.data.first()
        val interval = prefs[Keys.STATE_INTERVAL] ?: 0L
        val count = prefs[Keys.STATE_COUNT] ?: 0L
        val timestamp = prefs[Keys.STATE_TIMESTAMP] // Chargement du timestamp
        Log.d("DataStoreManager", "State loaded: interval=$interval, count=$count, timestamp=$timestamp")
        return Triple(interval, count, timestamp)
    }

    // Anciennes méthodes, peuvent être fusionnées dans saveStateWithTimestamp si non nécessaires ailleurs
    suspend fun setLastCigaretteTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_CIG_TIME] = time
        }
    }

    suspend fun loadLastCigaretteTime(): Long? {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.LAST_CIG_TIME]
    }

    suspend fun setNextCigaretteTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NEXT_CIG_TIME] = time
        }
    }

    suspend fun loadNextCigaretteTime(): Long? {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.NEXT_CIG_TIME]
    }

    suspend fun saveDailyReport(report: DailyReport) {
        context.dataStore.edit { prefs ->
            val existingReportsString = prefs[Keys.DAILY_REPORTS] ?: ""
            val existingReports = DailyReport.deserializeList(existingReportsString).toMutableList()

            // Supprimer l'ancien rapport du même type et de la même date si existant
            existingReports.removeIf { it.date == report.date && it.type == report.type }
            existingReports.add(report)

            prefs[Keys.DAILY_REPORTS] = DailyReport.serializeList(existingReports)
            Log.d("DataStoreManager", "Daily report saved for date ${report.date}, type ${report.type}")
        }
    }

    suspend fun loadAllDailyReports(): List<DailyReport> {
        val prefs = context.dataStore.data.first()
        val raw = prefs[Keys.DAILY_REPORTS]
        return if (raw != null) DailyReport.deserializeList(raw) else emptyList()
    }

    suspend fun addDailyDepassement(date: String, depassement: Long) {
        val key = stringPreferencesKey("depassements_$date")
        val prefs = context.dataStore.data.first()
        val existing = prefs[key]
        val currentList = existing?.split(",")?.mapNotNull { it.toLongOrNull() }?.toMutableList() ?: mutableListOf()
        currentList.add(depassement)
        val serialized = currentList.joinToString(",")
        context.dataStore.edit { it[key] = serialized }
        Log.d("DataStoreManager", "Added depassement $depassement ms for date $date")
    }

    suspend fun getDailyDepassements(date: String): List<Long> {
        val key = stringPreferencesKey("depassements_$date")
        val prefs = context.dataStore.data.first()
        return prefs[key]?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
    }

    suspend fun getMoyenneTenue(): Long {
        val allReports = loadAllDailyReports().filter { it.type == "daily" }
        if (allReports.isEmpty()) return 0L

        // Calculer l'intervalle idéal basé sur les paramètres actuels
        val settings = loadSettings()
        val idealInterval = if (settings.modeSevrage == Settings.MODE_ESPACEMENT) {
            (settings.espacementHeures * 60 + settings.espacementMinutes) * 60 * 1000L
        } else { // MODE_OBJECTIF
            val dailyMilliseconds = 24 * 60 * 60 * 1000L
            if (settings.objectifParJour > 0) {
                dailyMilliseconds / settings.objectifParJour
            } else {
                0L
            }
        }

        // Moyenne du temps réel tenu par rapport à l'intervalle idéal
        // Si idealInterval est 0, ou si la tenue est inférieure à l'idéal, on considère 0
        // Sinon, la moyenne est (intervalle réel - intervalle idéal)
        val tenueDurations = allReports.mapNotNull { report ->
            if (report.cigarettesSmoked > 0 && idealInterval > 0 && report.avgIntervalMs > idealInterval) {
                report.avgIntervalMs - idealInterval
            } else {
                null
            }
        }

        return if (tenueDurations.isNotEmpty()) {
            tenueDurations.average().toLong()
        } else {
            0L
        }
    }
}

fun Settings.serialize(): String = listOf(
    prixPaquet,
    cigarettesParPaquet,
    espacementHeures,
    espacementMinutes,
    cigarettesHabituelles,
    objectifParJour, // Ajouté
    modeSevrage // Ajouté
).joinToString(";")

fun deserializeSettings(serialized: String): Settings {
    val parts = serialized.split(";")
    return Settings(
        prixPaquet = parts[0].toFloat(),
        cigarettesParPaquet = parts[1].toInt(),
        espacementHeures = parts[2].toInt(),
        espacementMinutes = parts[3].toInt(),
        cigarettesHabituelles = parts[4].toInt(),
        objectifParJour = parts.getOrNull(5)?.toInt() ?: 20, // Gérer le cas où l'ancien format n'a pas cet élément
        modeSevrage = parts.getOrNull(6) ?: Settings.MODE_OBJECTIF // Gérer le cas où l'ancien format n'a pas cet élément
    )
}