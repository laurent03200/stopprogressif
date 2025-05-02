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

    private val _shouldSuggestAugmentation = MutableStateFlow(false)
    val shouldSuggestAugmentation: StateFlow<Boolean> = _shouldSuggestAugmentation

    private companion object {
        private const val ONE_SECOND = 1_000L
        private const val SUGGESTION_THRESHOLD_MS = 15 * 60_000L
        private const val NB_INTERVALS_BEFORE_SUGGESTION = 3
    }

    private val ecartsList = mutableListOf<Long>()
    private var isTimerRunning = false

    // Pour gérer la remise à zéro quotidienne
    private var lastDayChecked: Long = -1L

    init {
        // Restauration de l'état précédent + démarrage du timer
        val (savedTemps, savedCigarettes, timestamp) = dataStore.loadStateWithTimestamp()
        val now = System.currentTimeMillis()
        _tempsRestant.value = savedTemps - (now - timestamp)
        _cigarettesFumees.value = savedCigarettes
        startTimer()
    }

    /**
     * Sauvegarde des nouveaux réglages et réinitialisation immédiate du timer
     */
    fun saveSettings(newSettings: SettingsData) {
        _settingsData.value = newSettings
        resetTimer()
        viewModelScope.launch {
            dataStore.saveSettings(newSettings)
        }
    }

    fun fumerUneCigarette() {
        val interval = getInitialIntervalle()
        val ecoule = interval - _tempsRestant.value
        if (ecoule > SUGGESTION_THRESHOLD_MS) {
            ecartsList.add(ecoule)
            if (ecartsList.size >= NB_INTERVALS_BEFORE_SUGGESTION) {
                _shouldSuggestAugmentation.value = true
                ecartsList.clear()
            }
        }
        _cigarettesFumees.value += 1
        resetTimer()
    }

    fun annulerDerniereCigarette() {
        if (_cigarettesFumees.value > 0) _cigarettesFumees.value -= 1
    }

    fun clearSuggestionFlag() {
        _shouldSuggestAugmentation.value = false
    }

    /** Réinitialise le timer selon le mode actuel */
    private fun resetTimer() {
        val initial = getInitialIntervalle()
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
                delay(ONE_SECOND)
                tick()
            }
        }
    }

    /** Boucle principale : décrément du timer, reset quotidien et hors plages */
    private suspend fun tick() {
        val now = System.currentTimeMillis()
        // 1) Reset quotidien si on change de jour
        val today = now / TimeUnit.DAYS.toMillis(1)
        if (today != lastDayChecked) {
            lastDayChecked = today
            resetDaily()
            return
        }
        val s = _settingsData.value
        // 2) Vérifie plage active
        val (startMs, endMs) = boundsMillis(s)
        val nowOfDay = nowOfDayMillis()
        if (nowOfDay < startMs || nowOfDay > endMs) {
            // Hors plage active, on stoppe le tick
            return
        }
        // 3) Décrément normal
        val lastUpdate = dataStore.getLastUpdateTime()
        val delta = now - lastUpdate
        val updated = _tempsRestant.value - delta
        _tempsRestant.value = updated
        dataStore.saveStateWithTimestamp(updated, _cigarettesFumees.value)
    }

    /** Millisecondes écoulées depuis minuit */
    private fun nowOfDayMillis(): Long =
        System.currentTimeMillis() % TimeUnit.DAYS.toMillis(1)

    /** Retourne les bornes [début, fin] de la plage active en ms */
    private fun boundsMillis(s: SettingsData): Pair<Long, Long> {
        val start = TimeUnit.HOURS.toMillis(s.heuresDebut.toLong()) +
                TimeUnit.MINUTES.toMillis(s.minutesDebut.toLong())
        val end   = TimeUnit.HOURS.toMillis(s.heuresFin.toLong()) +
                TimeUnit.MINUTES.toMillis(s.minutesFin.toLong())
        return start to end
    }

    /** Reset complet à zéro pour une nouvelle journée */
    private fun resetDaily() {
        _cigarettesFumees.value = 0
        _shouldSuggestAugmentation.value = false
        ecartsList.clear()
        val initial = getInitialIntervalle()
        _tempsRestant.value = initial
        viewModelScope.launch {
            dataStore.saveStateWithTimestamp(initial, 0)
        }
    }

    /** Calcule l’intervalle initial selon le mode */
    fun getInitialIntervalle(): Long {
        val s = _settingsData.value
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

    /** Calcule la durée de la plage active en ms, gère le chevauchement de minuit */
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
