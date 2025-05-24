package com.example.stopprogressif.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.stopprogressif.NotificationHelper
import com.example.stopprogressif.model.DailyReport
import com.example.stopprogressif.data.DataStoreManager
import com.example.stopprogressif.data.Settings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import com.example.stopprogressif.timer.TimerController
import kotlin.math.max
import kotlin.math.roundToLong
import kotlin.math.abs

class ProgressifViewModel(application: Application) : AndroidViewModel(application) {

    private val _historique = MutableStateFlow<List<DailyReport>>(emptyList())
    val historique: StateFlow<List<DailyReport>> = _historique.asStateFlow()

    private val _semaineMoyenne = MutableStateFlow(0L)
    val semaineMoyenne: StateFlow<Long> = _semaineMoyenne.asStateFlow()

    private val _moisMoyenne = MutableStateFlow(0L)
    val moisMoyenne: StateFlow<Long> = _moisMoyenne.asStateFlow()

    private val _lastCigaretteTime = MutableStateFlow<Long?>(null)
    val lastCigaretteTime: StateFlow<Long?> = _lastCigaretteTime.asStateFlow()

    private val _nextCigaretteTime = MutableStateFlow<Long?>(null)
    val nextCigaretteTime: StateFlow<Long?> = _nextCigaretteTime.asStateFlow()

    private var timerNotificationSent = false
    private var depasseTimerActive: Boolean = false

    private val dataStore = DataStoreManager(application)

    private val _settingsData = MutableStateFlow(Settings())
    val settingsData: StateFlow<Settings> = _settingsData.asStateFlow()

    private val _cigarettesFumees = MutableStateFlow(0L)
    val cigarettesFumees: StateFlow<Long> = _cigarettesFumees.asStateFlow()

    private val _tempsRestant = MutableStateFlow(0L)
    val tempsRestant: StateFlow<Long> = _tempsRestant.asStateFlow()

    private val _tempsDepasse = MutableStateFlow(0L)
    val tempsDepasse: StateFlow<Long> = _tempsDepasse.asStateFlow()

    private val _cercleColor = MutableStateFlow(Color.Red)
    val cercleColor: StateFlow<Color> = _cercleColor.asStateFlow()

    init {
        viewModelScope.launch { // Wrapped the suspend call in a coroutine
            val today = java.time.LocalDate.now().toString()
            val saved = dataStore.loadAllDailyReports().find { it.date == today && it.type == "daily" }
            if (saved != null && saved.avgTimeExceededMs > 0) {
                _tempsDepasse.value = -1L // verrouiller à la relance
                Log.d("DEBUG_PROG", "🔒 Verrou restauré au démarrage : ${saved.avgTimeExceededMs} ms")
            }

            val allReports = dataStore.loadAllDailyReports()
            _historique.value = allReports

            _semaineMoyenne.value = allReports
                .filter { it.type == "daily" }
                .takeLast(7)
                .map { it.avgTimeExceededMs }
                .takeIf { it.isNotEmpty() }
                ?.average()?.toLong() ?: 0L

            _moisMoyenne.value = allReports
                .filter { it.type == "daily" }
                .takeLast(30)
                .map { it.avgTimeExceededMs }
                .takeIf { it.isNotEmpty() }
                ?.average()?.toLong() ?: 0L

            _settingsData.value = dataStore.loadSettings()
            _lastCigaretteTime.value = dataStore.getLastCigaretteTime()
            val interval = computeInterval()
            _nextCigaretteTime.value = _lastCigaretteTime.value?.plus(interval)
            _historique.value = dataStore.loadAllDailyReports()
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val (savedInterval, savedCount, lastUpdate) = dataStore.loadStateWithTimestamp()
            val now = System.currentTimeMillis()

            _cigarettesFumees.value = savedCount

            val currentNextCigTime = _nextCigaretteTime.value ?: (now + computeInterval())
            val remainingTime = (currentNextCigTime - now)
            TimerController.start(remainingTime)
            Log.d("DEBUG_PROG", "🧮 RemainingTime: $remainingTime, Now: $now, NextCig: $currentNextCigTime")
            _tempsRestant.value = remainingTime

            Log.d("DEBUG_PROG", "⏱️ Calcul: _tempsDepasse = ${_tempsDepasse.value}")

            _tempsRestant.value = remainingTime

            if (remainingTime <= 0) {
                if (_tempsDepasse.value != -1L) {
                    _tempsDepasse.value = abs(remainingTime)
                }

                if (_tempsDepasse.value > 0 && _tempsDepasse.value != -1L) {
                    val today = java.time.LocalDate.now().toString()
                    val alreadySaved = _historique.value.any {
                        it.date == today && it.type == "daily" && it.avgTimeExceededMs > 0
                    }
                    if (!alreadySaved) {
                        Log.d("DEBUG_PROG", "💾 Auto-saving depassement = ${_tempsDepasse.value}")
                        dataStore.saveOrUpdateDailyReportWithDepasse(_tempsDepasse.value)
                        _tempsDepasse.value = -1L // verrouillage
                    }
                }

                Log.d("DEBUG_PROG", "📈 REFRESH _tempsDepasse = ${_tempsDepasse.value}")
                _cercleColor.value = Color(0xFF00FF00)
                if (!timerNotificationSent) {
                    NotificationHelper(getApplication()).sendTimerFinishedNotification(now)
                    timerNotificationSent = true
                }
            } else {
                _tempsDepasse.value = 0L
                _cercleColor.value = Color.Red
                timerNotificationSent = false
            }
            _historique.value = dataStore.loadAllDailyReports()
        }
    }

    fun fumerUneCigarette() {
        viewModelScope.launch {
            val interval = computeInterval()
            val newCount = _cigarettesFumees.value + 1L
            depasseTimerActive = false
            val now = System.currentTimeMillis()

            _cigarettesFumees.value = newCount
            _lastCigaretteTime.value = now
            _nextCigaretteTime.value = now + interval
            dataStore.setNextCigaretteTime(now + interval)

            val tempsTenue = (now - (_nextCigaretteTime.value?.minus(interval) ?: now)).coerceAtLeast(0L)
            dataStore.appendDureeTenue(tempsTenue)
            timerNotificationSent = false

            dataStore.saveOrUpdateDailyReportWithDepasse(_tempsDepasse.value)
            _tempsDepasse.value = -1L // verrouillage
            dataStore.saveStateWithTimestamp(interval, newCount)
            dataStore.setLastCigaretteTime(now)

            enregistrerOuMaj(newCount)
            _historique.value = dataStore.loadAllDailyReports()
            refresh()
        }
    }

    private fun enregistrerOuMaj(nombreCigarettes: Long) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val reports = dataStore.loadAllDailyReports().toMutableList()

            val rapportDuJour = reports.find { it.date == today && it.type == "daily" }

            val updatedReport = if (rapportDuJour != null) {
                rapportDuJour.copy(cigarettesSmoked = nombreCigarettes.toInt())
            } else {
                val settings = _settingsData.value
                val prixUnitaire = settings.prixPaquet / max(settings.cigarettesParPaquet, 1)
                val economieCents = (prixUnitaire * nombreCigarettes * 100).roundToLong()

                DailyReport(
                    date = today,
                    cigarettesSmoked = nombreCigarettes.toInt(),
                    avgTimeExceededMs = 0,
                    avgIntervalMs = 0,
                    moneySavedCents = economieCents,
                    type = "daily"
                )
            }
            dataStore.saveDailyReport(updatedReport)
        }
    }

    fun annulerDerniereCigarette() {
        viewModelScope.launch {
            if (_cigarettesFumees.value > 0) {
                val newCount = _cigarettesFumees.value - 1L
                dataStore.saveStateWithTimestamp(_tempsRestant.value, newCount)
                _cigarettesFumees.value = newCount
                enregistrerOuMaj(newCount)
                refresh()
                _historique.value = dataStore.loadAllDailyReports()
            }
        }
    }

    fun resetDaily() {
        viewModelScope.launch {
            dataStore.saveStateWithTimestamp(0L, 0L)
            _cigarettesFumees.value = 0L
            _lastCigaretteTime.value = null
            _nextCigaretteTime.value = null
            timerNotificationSent = false
            enregistrerOuMaj(0L)
            _historique.value = dataStore.loadAllDailyReports()
            refresh()
        }
    }

    private fun computeInterval(): Long {
        val settings = _settingsData.value
        return (settings.espacementHeures * 60 + settings.espacementMinutes) * 60_000L
    }

    fun reloadStateIfReset() {
        viewModelScope.launch {
            val (savedInterval, savedCount, savedTimestamp) = dataStore.loadStateWithTimestamp()
            _cigarettesFumees.value = savedCount

            val currentRemaining = _tempsRestant.value
            _tempsDepasse.value = if (currentRemaining > 0L) 0L else abs(currentRemaining)

            _lastCigaretteTime.value = dataStore.getLastCigaretteTime()
            _lastCigaretteTime.value?.let { last ->
                _nextCigaretteTime.value = last + computeInterval()
            } ?: run {
                _nextCigaretteTime.value = System.currentTimeMillis() + computeInterval()
            }
            timerNotificationSent = false
            _historique.value = dataStore.loadAllDailyReports()
            refresh()
        }
    }

    suspend fun getAllDailyReports(): List<DailyReport> {
        return dataStore.loadAllDailyReports()
    }

    fun saveSettings(newSettings: Settings) {
        _settingsData.value = newSettings
        viewModelScope.launch {
            dataStore.saveSettings(newSettings)
            _historique.value = dataStore.loadAllDailyReports()
            refresh()
        }
    }

    private fun startDepasseTimer() {
        // Placeholder pour logique future
    }
}
