package com.example.stopprogressif.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow
import java.io.IOException

private val Context.dataStore by preferencesDataStore("user_settings")

class SettingsDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val INTERVAL = longPreferencesKey("interval_minutes")
        val PRICE = doublePreferencesKey("price_per_pack")
        val CIGARETTES = intPreferencesKey("cigarettes_per_pack")
        val MODE = stringPreferencesKey("tracking_mode")
    }

    val interval: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[INTERVAL] ?: 90L }

    val pricePerPack: Flow<Double> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PRICE] ?: 10.0 }

    val cigarettesPerPack: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[CIGARETTES] ?: 20 }

    val trackingMode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[MODE] ?: "progressif" }

    suspend fun saveInterval(value: Long) {
        dataStore.edit { it[INTERVAL] = value }
    }

    suspend fun savePrice(value: Double) {
        dataStore.edit { it[PRICE] = value }
    }

    suspend fun saveCigarettesPerPack(value: Int) {
        dataStore.edit { it[CIGARETTES] = value }
    }

    suspend fun saveTrackingMode(value: String) {
        dataStore.edit { it[MODE] = value }
    }
}