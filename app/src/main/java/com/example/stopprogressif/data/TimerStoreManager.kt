package com.example.stopprogressif.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "timer_preferences")

class TimerStoreManager(private val context: Context) {

    companion object {
        val INTERVAL_KEY = longPreferencesKey("interval")
        val LAST_CIGARETTE_TIME_KEY = longPreferencesKey("last_cigarette_time")    }

    private val dataStore = context.dataStore

    val userInterval: Flow<Long> = dataStore.data.map { preferences ->
        preferences[INTERVAL_KEY] ?: 60 * 60 * 1000L // default 1 hour
    }

    val lastCigaretteTime: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[LAST_CIGARETTE_TIME_KEY]
    }

    suspend fun saveLastCigaretteTime(timeMillis: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_CIGARETTE_TIME_KEY] = timeMillis
        }
    }

    suspend fun saveUserInterval(duration: Long) {
        dataStore.edit { preferences ->
            preferences[INTERVAL_KEY] = duration
        }
    }    suspend fun getUserInterval(): Long {
        return dataStore.data.map { it[INTERVAL_KEY] ?: 60 * 60 * 1000L }.first()
    }
}