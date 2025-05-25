package com.example.stopprogressif.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stopprogressif.NotificationHelper
import com.example.stopprogressif.data.DataStoreManager
import com.example.stopprogressif.data.Settings
import com.example.stopprogressif.model.DailyReport
import com.example.stopprogressif.receiver.AlarmReceiver
import com.example.stopprogressif.timer.TimerController
import com.example.stopprogressif.timer.TimerService // <-- MODIFICATION ICI : package correct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

@HiltViewModel
open class ProgressifViewModel @Inject constructor(
    application: Application,
    private val dataStore: DataStoreManager,
    private val notificationHelper: NotificationHelper,
    private val timerController: TimerController // TimerController maintenant injecté
) : AndroidViewModel(application) {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _settingsData = MutableStateFlow(Settings())
    val settingsData: StateFlow<Settings> = _settingsData.asStateFlow()

    private val _timeLeftFormatted = MutableStateFlow("00:00:00")
    val timeLeftFormatted: StateFlow<String> = _timeLeftFormatted.asStateFlow()

    private val _currentCigarettesCount = MutableStateFlow(0L)
    val currentCigarettesCount: StateFlow<Long> = _currentCigarettesCount.asStateFlow()

    private val _lastCigaretteTime = MutableStateFlow<Long?>(null)
    val lastCigaretteTime: StateFlow<Long?> = _lastCigaretteTime.asStateFlow()

    private val _nextCigaretteTime = MutableStateFlow<Long?>(null)
    val nextCigaretteTime: StateFlow<Long?> = _nextCigaretteTime.asStateFlow()

    private val _historique = MutableStateFlow<List<DailyReport>>(emptyList())
    val historique: StateFlow<List<DailyReport>> = _historique.asStateFlow()

    private val _semaineMoyenne = MutableStateFlow(0L)
    val semaineMoyenne: StateFlow<Long> = _semaineMoyenne.asStateFlow()

    private val _moisMoyenne = MutableStateFlow(0L)
    val moisMoyenne: StateFlow<Long> = _moisMoyenne.asStateFlow()

    private val _moyenneTenue = MutableStateFlow(0L)
    val moyenneTenue: StateFlow<Long> = _moyenneTenue.asStateFlow()

    private var timerNotificationSent = false // Pour éviter d'envoyer plusieurs notifications

    init {
        Log.d("ProgressifViewModel", "ViewModel initialized")
        viewModelScope.launch {
            // Charge les paramètres au démarrage du ViewModel
            _settingsData.value = dataStore.loadSettings()
            Log.d("ProgressifViewModel", "Settings loaded: ${_settingsData.value}")

            // Charge l'état actuel et met à jour les observables
            val (interval, count, timestamp) = dataStore.loadStateWithTimestamp()
            _currentCigarettesCount.value = count
            _lastCigaretteTime.value = timestamp
            _nextCigaretteTime.value = timestamp?.plus(interval) // Use safe call and plus

            // Charge l'historique des rapports quotidiens
            _historique.value = dataStore.loadAllDailyReports()
            calculerMoyennes() // Recalcule les moyennes après le chargement de l'historique

            // Démarrer l'observateur du timerController
            timerController.timeLeft.collect { timeLeft ->
                _timeLeftFormatted.value = formatMillisToHoursMinutesSeconds(timeLeft)
                if (timeLeft <= 0L && timerController.state.value == TimerController.TimerState.FINISHED && !timerNotificationSent) {
                    notificationHelper.sendTimerFinishedNotification(_lastCigaretteTime.value ?: System.currentTimeMillis())
                    timerNotificationSent = true
                }
            }
        }

        // Observer les changements dans les paramètres pour mettre à jour les intervalles
        viewModelScope.launch {
            settingsData.collect {
                // Si le mode de sevrage change ou les valeurs d'espacement/objectif changent
                // et si un timer est actif, recalculer le prochain temps de cigarette.
                refresh()
            }
        }

        // Observer l'état du timerController pour relancer le service si nécessaire
        viewModelScope.launch {
            timerController.state.collect { state ->
                val context = getApplication<Application>()
                if (state == TimerController.TimerState.RUNNING) {
                    // MODIFICATION ICI : Utilisation directe de TimerService après l'import correct
                    val serviceIntent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_START
                        putExtra(TimerService.EXTRA_INITIAL_TIME, timerController.timeLeft.value)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d("ProgressifViewModel", "TimerService démarré/relancé via ViewModel.")
                } else if (state == TimerController.TimerState.FINISHED || state == TimerController.TimerState.IDLE) {
                    // MODIFICATION ICI : Utilisation directe de TimerService après l'import correct
                    val serviceIntent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_STOP
                    }
                    context.stopService(serviceIntent)
                    Log.d("ProgressifViewModel", "TimerService arrêté via ViewModel.")
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Charger les dernières données depuis DataStore
            val (interval, count, timestamp) = dataStore.loadStateWithTimestamp()
            _settingsData.value = dataStore.loadSettings() // Settings loaded after state
            _currentCigarettesCount.value = count
            _lastCigaretteTime.value = timestamp
            _historique.value = dataStore.loadAllDailyReports()

            // Recalculer le prochain temps de cigarette
            _lastCigaretteTime.value?.let { last ->
                _nextCigaretteTime.value = last + computeInterval()
            } ?: run {
                // Si aucune dernière cigarette, le prochain temps est maintenant + intervalle
                _nextCigaretteTime.value = System.currentTimeMillis() + computeInterval()
            }

            // Mettre à jour le timerController si nécessaire
            _nextCigaretteTime.value?.let { nextTime ->
                val timeRemaining = max(0L, nextTime - System.currentTimeMillis())
                if (timerController.state.value != TimerController.TimerState.RUNNING || timerController.timeLeft.value != timeRemaining) {
                    if (timeRemaining > 0) {
                        timerController.start(timeRemaining)
                    } else {
                        timerController.stop()
                    }
                }
            }

            calculerMoyennes() // Recalcule les moyennes après le rafraîchissement
            _isRefreshing.value = false
            Log.d("ProgressifViewModel", "Données rafraîchies.")
        }
    }

    fun onCigaretteSmoked() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val (lastInterval, lastCount, _) = dataStore.loadStateWithTimestamp() // Timestamp not needed here

            // Calculer le temps réel passé depuis la dernière cigarette
            val actualInterval = now - (_lastCigaretteTime.value ?: now)
            val exceededTime = max(0L, abs(actualInterval - computeInterval()))

            // Mettre à jour le nombre de cigarettes et l'intervalle moyen
            val newCount = lastCount + 1
            val newInterval = if (lastCount == 0L) actualInterval else (lastInterval * lastCount + actualInterval) / newCount

            dataStore.saveStateWithTimestamp(newInterval, newCount, now) // Passed timestamp
            _currentCigarettesCount.value = newCount
            _lastCigaretteTime.value = now

            // Ajouter le dépassement au rapport quotidien
            val today = LocalDate.now().toString()
            dataStore.addDailyDepassement(today, exceededTime)

            // Recalculer le prochain temps de cigarette et démarrer le timer
            _nextCigaretteTime.value = now + computeInterval()
            _nextCigaretteTime.value?.let { nextTime ->
                val timeRemaining = nextTime - now
                timerController.start(timeRemaining)
                Log.d("ProgressifViewModel", "Prochaine cigarette autorisée à ${Date(nextTime)}")
            }

            timerNotificationSent = false // Réinitialiser le flag de notification

            // Rafraîchir l'historique et les statistiques
            _historique.value = dataStore.loadAllDailyReports()
            refreshStats()
        }
    }

    fun resetDailyProgress() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dataStore.saveStateWithTimestamp(0L, 0L, now) // Réinitialise les cigarettes et l'intervalle avec timestamp
            dataStore.setLastCigaretteTime(now)
            dataStore.setNextCigaretteTime(now + computeInterval()) // Réinitialise le prochain temps
            _currentCigarettesCount.value = 0
            _lastCigaretteTime.value = now
            _nextCigaretteTime.value = now + computeInterval()
            timerController.start(computeInterval())
            notificationHelper.sendDailyResetNotification() // Envoyer une notification de réinitialisation
            timerNotificationSent = false
            _historique.value = dataStore.loadAllDailyReports() // Recharge l'historique (peut être déjà fait par le worker)
            refreshStats() // Recalcule les statistiques
            Log.d("ProgressifViewModel", "Progression quotidienne réinitialisée.")
        }
    }

    fun saveSettings(newSettings: Settings) {
        _settingsData.value = newSettings
        viewModelScope.launch {
            dataStore.saveSettings(newSettings)
            // Après la sauvegarde des paramètres, rafraîchir pour que les changements soient pris en compte
            refresh()
            Log.d("ProgressifViewModel", "Paramètres sauvegardés et rafraîchis: $newSettings")
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            val moyenne = dataStore.getMoyenneTenue()
            _moyenneTenue.value = moyenne
        }
    }

    private fun calculerMoyennes() {
        viewModelScope.launch {
            val reports = dataStore.loadAllDailyReports().filter { it.type == "daily" }

            _semaineMoyenne.value = reports.takeLast(7)
                .map { it.avgTimeExceededMs }
                .takeIf { it.isNotEmpty() }
                ?.average()?.roundToLong() ?: 0L

            _moisMoyenne.value = reports.takeLast(30)
                .map { it.avgTimeExceededMs }
                .takeIf { it.isNotEmpty() }
                ?.average()?.roundToLong() ?: 0L
        }
    }

    private fun computeInterval(): Long {
        val settings = _settingsData.value
        val intervalMillis = if (settings.modeSevrage == Settings.MODE_ESPACEMENT) {
            (settings.espacementHeures * 60 + settings.espacementMinutes) * 60 * 1000L
        } else { // MODE_OBJECTIF
            // Calculer l'intervalle basé sur l'objectif et les cigarettes habituelles
            val dailyMilliseconds = 24 * 60 * 60 * 1000L
            if (settings.objectifParJour > 0) {
                dailyMilliseconds / settings.objectifParJour
            } else {
                0L // Éviter la division par zéro
            }
        }
        Log.d("ProgressifViewModel", "Intervalle calculé: ${intervalMillis / 1000} secondes")
        return intervalMillis
    }

    private fun formatMillisToHoursMinutesSeconds(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        // Assurez-vous que le timer est arrêté lorsque le ViewModel est détruit
        timerController.stop()
        cancelAlarm() // Annule toute alarme pendante lorsque le ViewModel est détruit
        Log.d("ProgressifViewModel", "ViewModel cleared and timer stopped.")
    }

    // Gestion des alarmes pour des rappels précis (en dehors du service)
    private fun scheduleAlarm(timeInMillis: Long) {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(getApplication(), AlarmReceiver::class.java).apply {
            action = "com.example.stopprogressif.ACTION_TRIGGER_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
        Log.d("ProgressifViewModel", "Alarme programmée pour ${Date(timeInMillis)}")
    }

    private fun cancelAlarm() {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(getApplication(), AlarmReceiver::class.java).apply {
            action = "com.example.stopprogressif.ACTION_TRIGGER_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("ProgressifViewModel", "Alarme annulée.")
    }
}