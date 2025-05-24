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
        val STATE_TIMESTAMP = longPreferencesKey("state_timestamp")
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

    suspend fun saveStateWithTimestamp(interval: Long, count: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STATE_INTERVAL] = interval
            prefs[Keys.STATE_COUNT] = count
            prefs[Keys.STATE_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun loadStateWithTimestamp(): Triple<Long, Long, Long> {
        val prefs = context.dataStore.data.first()
        val interval = prefs[Keys.STATE_INTERVAL] ?: 0L
        val count: Long = try {
            prefs[Keys.STATE_COUNT] ?: 0L
        } catch (e: ClassCastException) {
            val oldIntKey = intPreferencesKey("state_count")
            prefs[oldIntKey]?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
        val timestamp = prefs[Keys.STATE_TIMESTAMP] ?: System.currentTimeMillis()
        return Triple(interval, count, timestamp)
    }

    suspend fun getLastCigaretteTime(): Long? {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.LAST_CIG_TIME]
    }

    suspend fun setLastCigaretteTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_CIG_TIME] = time
        }
    }

    suspend fun getNextCigaretteTime(): Long? {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.NEXT_CIG_TIME]
    }

    suspend fun setNextCigaretteTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NEXT_CIG_TIME] = time
        }
    }

    suspend fun saveDailyReport(report: DailyReport) {
        val existing = loadAllDailyReports().toMutableList()
        existing.removeAll { it.date == report.date && it.type == report.type }
        existing.add(report)
        val serialized = DailyReport.serializeList(existing)
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_REPORTS] = serialized
        }
    }

    suspend fun loadAllDailyReports(): List<DailyReport> {
        val prefs = context.dataStore.data.first()
        val raw = prefs[Keys.DAILY_REPORTS]
        return if (!raw.isNullOrBlank()) DailyReport.deserializeList(raw) else emptyList()
    }

    suspend fun resetDailyState() {
        saveStateWithTimestamp(0L, 0L)
    }

    suspend fun appendDureeTenue(temps: Long) {
        val prefs = context.dataStore.data.first()
        val existantesString = prefs[Keys.DUREES_TENUES]
        val liste = if (!existantesString.isNullOrBlank()) {
            existantesString.split(",").mapNotNull { it.toLongOrNull() }.toMutableList()
        } else {
            mutableListOf()
        }
        liste.add(temps)
        val serialized = liste.joinToString(",")
        context.dataStore.edit { it[Keys.DUREES_TENUES] = serialized }
    }

    suspend fun getMoyenneTenue(): Long {
        val prefs = context.dataStore.data.first()
        val existantes = prefs[Keys.DUREES_TENUES]
        val liste = if (!existantes.isNullOrBlank()) {
            existantes.split(",").mapNotNull { it.toLongOrNull() }
        } else {
            emptyList()
        }
        return if (liste.isNotEmpty()) liste.sum() / liste.size else 0L
    }

    suspend fun saveOrUpdateDailyReportWithDepasse(tempsDepasse: Long) {
        val today = LocalDate.now().toString()
        val existing = loadAllDailyReports().toMutableList()
        val index = existing.indexOfFirst { it.date == today && it.type == "daily" }

        if (index >= 0) {
            val original = existing[index]
            existing[index] = original.copy(avgTimeExceededMs = tempsDepasse)
        } else {
            existing.add(DailyReport(
                date = today,
                cigarettesSmoked = 0,
                avgTimeExceededMs = tempsDepasse,
                avgIntervalMs = 0,
                moneySavedCents = 0,
                type = "daily"
            ))
        }

        Log.d("DEBUG_PROG", "✅ SAVE DEBUG → Sauvegarde depassement $tempsDepasse ms pour date $today")

        val serialized = DailyReport.serializeList(existing)
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_REPORTS] = serialized
        }
    }
}

fun Settings.serialize(): String = listOf(
    prixPaquet,
    cigarettesParPaquet,
    espacementHeures,
    espacementMinutes,
    cigarettesHabituelles
).joinToString(";")

fun deserializeSettings(serialized: String): Settings {
    val parts = serialized.split(";")
    return Settings(
        prixPaquet = parts[0].toFloat(),
        cigarettesParPaquet = parts[1].toInt(),
        espacementHeures = parts[2].toInt(),
        espacementMinutes = parts[3].toInt(),
        cigarettesHabituelles = parts[4].toInt()
    )
}
