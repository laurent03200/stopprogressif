package com.stopprogressif

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.math.max

class ProgressifViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = DataStoreManager(application)

    private val _settingsData = MutableStateFlow(runBlocking { dataStore.loadSettings() })
    val settingsData: StateFlow<SettingsData> = _settingsData

    private val _cigarettesFumees = MutableStateFlow(0)
    val cigarettesFumees: StateFlow<Int> = _cigarettesFumees

    private val _tempsRestant = MutableStateFlow(0L)
    val tempsRestant: StateFlow<Long> = _tempsRestant

    private var isTimerRunning = false

    init {
        // Restaure l'état et démarre le timer
        val (savedTemps, savedCig, timestamp) = dataStore.loadStateWithTimestamp()
        val now = System.currentTimeMillis()
        _tempsRestant.value = savedTemps - (now - timestamp)
        _cigarettesFumees.value = savedCig
        startTimer()
    }

    /**
     * Met à jour les settings, recalcule et réinitialise
     * le timer, puis persiste en arrière-plan.
     */
    fun saveSettings(newSettings: SettingsData) {
        // 1) Met à jour immédiatement
        _settingsData.value = newSettings

        // 2) Recalcule l'intervalle et remet à zéro
        val initial = computeInterval(newSettings)
        _tempsRestant.value = initial

        // 3) Persiste settings + nouvel état timer
        viewModelScope.launch {
            dataStore.saveSettings(newSettings)
            dataStore.saveStateWithTimestamp(initial, _cigarettesFumees.value)
        }
    }

    fun fumerUneCigarette() {
        _cigarettesFumees.value += 1
        resetTimer()
    }

    fun annulerDerniereCigarette() {
        if (_cigarettesFumees.value > 0) _cigarettesFumees.value -= 1
    }

    private fun resetTimer() {
        val initial = computeInterval(_settingsData.value)
        _tempsRestant.value = initial
        viewModelScope.launch {
            dataStore.saveStateWithTimestamp(initial, _cigarettesFumees.value)
        }
    }

    private fun startTimer() {
        if (isTimerRunning) return
        isTimerRunning = true
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val now = System.currentTimeMillis()
                val last = dataStore.getLastUpdateTime()
                val diff = now - last
                val updated = _tempsRestant.value - diff
                _tempsRestant.value = updated
                dataStore.saveStateWithTimestamp(updated, _cigarettesFumees.value)
            }
        }
    }

    /**
     * Calcule l’intervalle en ms selon le mode :
     * – OBJECTIF : (durée active) / objectifParJour
     * – INTERVALLE : heuresEntreCigarettes + minutesEntreCigarettes
     */
    private fun computeInterval(s: SettingsData): Long {
        return if (s.mode == SettingsData.MODE_OBJECTIF) {
            val window = computeActiveWindowMillis(
                s.heuresDebut, s.minutesDebut,
                s.heuresFin,   s.minutesFin
            )
            window / max(1, s.objectifParJour)
        } else {
            val totalMin = max(1, s.heuresEntreCigarettes * 60 + s.minutesEntreCigarettes)
            TimeUnit.MINUTES.toMillis(totalMin.toLong())
        }
    }

    /**
     * Calcule la durée (ms) entre début et fin de plage horaire,
     * gère le chevauchement sur minuit.
     */
    private fun computeActiveWindowMillis(
        startH: Int, startM: Int,
        endH: Int,   endM: Int
    ): Long {
        val startMs = TimeUnit.HOURS.toMillis(startH.toLong()) +
                TimeUnit.MINUTES.toMillis(startM.toLong())
        val endMs   = TimeUnit.HOURS.toMillis(endH.toLong()) +
                TimeUnit.MINUTES.toMillis(endM.toLong())
        return if (endMs > startMs) {
            endMs - startMs
        } else {
            TimeUnit.DAYS.toMillis(1) - startMs + endMs
        }
    }
}
