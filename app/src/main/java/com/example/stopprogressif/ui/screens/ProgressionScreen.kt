package com.example.stopprogressif.viewmodel

import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import java.util.*
import com.example.stopprogressif.receiver.AlarmReceiver

class ProgressifViewModel(application: Application) : AndroidViewModel(application) {

    private val _historique = MutableStateFlow<List<DailyReport>>(emptyList())
    val historique: StateFlow<List<DailyReport>> = _historique.asStateFlow()

    private val _semaineMoyenne = MutableStateFlow(0L)
    val semaineMoyenne: StateFlow<Long> = _semaineMoyenne.asStateFlow()

    private val _moisMoyenne = MutableStateFlow(0L)
    val moisMoyenne: StateFlow<Long> = _moisMoyenne.asStateFlow()

    private val dataStore = DataStoreManager(application.applicationContext)
    private val notificationHelper = NotificationHelper(application.applicationContext)

    private val _settingsData = MutableStateFlow(Settings())
    val settingsData: StateFlow<Settings> = _settingsData.asStateFlow()

    private val _tempsRestant = MutableStateFlow(0L)
    val tempsRestant: StateFlow<Long> = _tempsRestant.asStateFlow()

    private val _tempsDepasse = MutableStateFlow(0L)
    val tempsDepasse: StateFlow<Long> = _tempsDepasse.asStateFlow()

    private val _lastCigaretteTime = MutableStateFlow<Long?>(null)
    val lastCigaretteTime: StateFlow<Long?> = _lastCigaretteTime.asStateFlow()

    private val _nextCigaretteTime = MutableStateFlow<Long?>(null)
    val nextCigaretteTime: StateFlow<Long?> = _nextCigaretteTime.asStateFlow()

    private val _cigarettesFumees = MutableStateFlow(0)
    val cigarettesFumees: StateFlow<Int> = _cigarettesFumees.asStateFlow()

    private val _economieActuelle = MutableStateFlow(0f)
    val economieActuelle: StateFlow<Float> = _economieActuelle.asStateFlow()

    init {
        // Initialisation de l'état du timer au démarrage du ViewModel
        viewModelScope.launch {
            _settingsData.value = dataStore.loadSettings()
            _lastCigaretteTime.value = dataStore.loadLastCigaretteTime()
            _nextCigaretteTime.value = dataStore.loadNextCigaretteTime()

            // Démarrer le timer si une prochaine cigarette est déjà programmée
            _nextCigaretteTime.value?.let { nextTime ->
                val remaining = nextTime - System.currentTimeMillis()
                if (remaining > 0) {
                    TimerController.start(remaining)
                } else {
                    TimerController.stop()
                }
            }

            // Observer l'état du timer et mettre à jour le temps restant
            TimerController.timeLeft.collect { timeLeft ->
                _tempsRestant.value = timeLeft
            }

            // Observer l'état du timer et réagir quand il est terminé
            TimerController.state.collect { state ->
                if (state == TimerController.TimerState.FINISHED) {
                    notificationHelper.sendTimerFinishedNotification(_lastCigaretteTime.value ?: System.currentTimeMillis())
                    startDepasseTimer() // Démarrer le timer de dépassement si le timer principal est terminé
                }
            }
            refresh()
        }
    }

    private var timerNotificationSent = false

    fun fumerUneCigarette() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()

            // Calcul du temps de dépassement (si le timer était fini)
            val tempsDepasseMs = max(0, abs(_tempsRestant.value)) // Utilisez abs pour convertir en positif

            dataStore.addDailyDepassement(LocalDate.now().toString(), tempsDepasseMs)

            // Mise à jour de l'état
            val (interval, count, _) = dataStore.loadStateWithTimestamp()
            val newCount = count + 1
            val newInterval = calculateNextInterval(interval, newCount.toInt())

            dataStore.saveStateWithTimestamp(newInterval, newCount)
            dataStore.setLastCigaretteTime(now)

            // Calcul du prochain temps de cigarette
            _nextCigaretteTime.value = now + computeInterval()

            // Démarrer le timer principal
            TimerController.start(computeInterval())

            // Réinitialiser le flag de notification de dépassement
            timerNotificationSent = false

            refresh()
        }
    }

    fun annulerDerniereCigarette() {
        viewModelScope.launch {
            // Récupérer les données actuelles
            val (interval, count, _) = dataStore.loadStateWithTimestamp()
            if (count > 0) {
                val newCount = count - 1
                val newInterval = calculatePreviousInterval(interval, newCount.toInt())

                dataStore.saveStateWithTimestamp(newInterval, newCount)

                // Récupérer le dernier temps de cigarette enregistré pour le réinitialiser si nécessaire
                val previousLastCigTime = dataStore.loadLastCigaretteTime() // Cela pourrait être complexe si on garde un historique complet
                // Pour simplifier, si on annule la dernière, on remet le timer à l'état précédent ou à zéro
                if (newCount == 0L) {
                    dataStore.setLastCigaretteTime(0L)
                    dataStore.setNextCigaretteTime(0L)
                    TimerController.stop()
                } else {
                    // Ici, la logique serait de retrouver l'avant-dernière cigarette, ce qui nécessite un historique plus complet.
                    // Pour l'instant, on se contente de réinitialiser le prochain temps en se basant sur le nouvel intervalle.
                    _nextCigaretteTime.value = System.currentTimeMillis() + newInterval
                    TimerController.start(newInterval)
                }
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _settingsData.value = dataStore.loadSettings()
            _cigarettesFumees.value = dataStore.loadStateWithTimestamp().second.toInt()
            _economieActuelle.value = calculateMoneySaved()
            _lastCigaretteTime.value = dataStore.loadLastCigaretteTime()
            _nextCigaretteTime.value = dataStore.loadNextCigaretteTime()

            _lastCigaretteTime.value?.let { last ->
                _nextCigaretteTime.value = last + computeInterval()
            } ?: run {
                _nextCigaretteTime.value = System.currentTimeMillis() + computeInterval()
            }
            timerNotificationSent = false
            _historique.value = dataStore.loadAllDailyReports()
            refreshStats()
        }
    }

    fun saveSettings(newSettings: Settings) {
        _settingsData.value = newSettings
        viewModelScope.launch {
            dataStore.saveSettings(newSettings)
            _historique.value = dataStore.loadAllDailyReports()
            refresh()
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            val moyenne = dataStore.getMoyenneTenue()
            _moyenneTenue.value = moyenne
        }
    }

    private fun startDepasseTimer() {
        // Placeholder for future implementation
    }

    private fun calculerMoyennes() {
        viewModelScope.launch {
            val reports = dataStore.loadAllDailyReports().filter { it.type == "daily" }

            _semaineMoyenne.value = reports.takeLast(7)
                .map { it.avgTimeExceededMs }
                .takeIf { it.isNotEmpty() }
                ?.average()?.toLong() ?: 0L

            _moisMoyenne.value = reports.takeLast(30)
                .map { it.avgTimeExceededMs }
                .takeIf { it.isNotEmpty() }
                ?.average()?.toLong() ?: 0L
        }
    }

    private fun computeInterval(): Long {
        val settings = _settingsData.value
        val cigarettesSmokedToday = _cigarettesFumees.value

        return when (settings.modeSevrage) {
            Settings.MODE_OBJECTIF -> {
                if (settings.objectifParJour == 0) return 0L

                val baseInterval = 24 * 3600 * 1000L / settings.objectifParJour
                val adjustmentFactor = when (cigarettesSmokedToday) {
                    in 0 until (settings.objectifParJour * 0.25).toInt() -> 1.2 // Plus long au début
                    in (settings.objectifParJour * 0.25).toInt() until (settings.objectifParJour * 0.75).toInt() -> 1.0 // Normal
                    else -> 0.8 // Plus court vers la fin pour éviter le dépassement
                }
                (baseInterval * adjustmentFactor).toLong()
            }
            Settings.MODE_ESPACEMENT -> {
                (settings.espacementHeures * 3600 + settings.espacementMinutes * 60) * 1000L
            }
            else -> 0L
        }
    }

    private fun calculateNextInterval(currentInterval: Long, cigarettesCount: Int): Long {
        val settings = _settingsData.value
        return when (settings.modeSevrage) {
            Settings.MODE_OBJECTIF -> {
                // Le nouvel intervalle est recalculé en fonction du nombre total de cigarettes fumées et de l'objectif
                // Ici, nous voulons maintenir une moyenne sur la journée.
                // Cela est géré par `computeInterval` en fonction de `cigarettesFumees`.
                computeInterval()
            }
            Settings.MODE_ESPACEMENT -> {
                // L'intervalle est fixe dans ce mode.
                (settings.espacementHeures * 3600 + settings.espacementMinutes * 60) * 1000L
            }
            else -> currentInterval
        }
    }

    private fun calculatePreviousInterval(currentInterval: Long, cigarettesCount: Int): Long {
        val settings = _settingsData.value
        return when (settings.modeSevrage) {
            Settings.MODE_OBJECTIF -> {
                // Pour l'annulation, on veut revenir à l'intervalle comme si la cigarette n'avait pas été fumée.
                // Cela signifie qu'on recalcule l'intervalle basé sur le `newCount`.
                computeInterval() // Va utiliser le `newCount` déjà mis à jour
            }
            Settings.MODE_ESPACEMENT -> {
                (settings.espacementHeures * 3600 + settings.espacementMinutes * 60) * 1000L
            }
            else -> currentInterval
        }
    }

    private fun calculateMoneySaved(): Float {
        val settings = _settingsData.value
        val cigarettesSmokedToday = _cigarettesFumees.value
        val costPerCigarette = settings.prixPaquet / settings.cigarettesParPaquet.toFloat()
        val totalCigarettesExpected = settings.cigarettesHabituelles

        val savedCigarettes = (totalCigarettesExpected - cigarettesSmokedToday).coerceAtLeast(0)
        return savedCigarettes * costPerCigarette
    }

    // Propriétés pour l'affichage formaté (dépendent des flows)
    val timeLeftFormatted: StateFlow<String> = _tempsRestant.map { millis ->
        val totalSeconds = abs(millis) / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "00:00:00")

    val currentCigarettesCount: StateFlow<Int> = _cigarettesFumees.asStateFlow()
    val currentEconomy: StateFlow<Float> = _economieActuelle.asStateFlow()

    // Nouvelle fonction pour la logique de "j'ai fumé"
    fun onCigaretteSmoked() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val (currentInterval, currentCount, _) = dataStore.loadStateWithTimestamp()

            // Calcul du temps de dépassement (si le timer était fini)
            // On prend la valeur actuelle de _tempsRestant et on s'assure qu'elle est positive si < 0
            val tempsDepasseMs = if (_tempsRestant.value < 0) abs(_tempsRestant.value) else 0L
            dataStore.addDailyDepassement(LocalDate.now().toString(), tempsDepasseMs)

            val newCount = currentCount + 1
            val nextInterval = computeInterval() // Recalculer l'intervalle en fonction des nouvelles settings et du nouveau nombre de cigarettes

            dataStore.saveStateWithTimestamp(nextInterval, newCount)
            dataStore.setLastCigaretteTime(now)
            dataStore.setNextCigaretteTime(now + nextInterval)

            // Démarrer le TimerService avec le nouvel intervalle
            startTimerService(nextInterval)

            refresh()
        }
    }

    // Nouvelle fonction pour la logique "annuler la dernière cigarette"
    fun cancelLastCigarette() {
        viewModelScope.launch {
            val (currentInterval, currentCount, _) = dataStore.loadStateWithTimestamp()

            if (currentCount > 0) {
                val newCount = currentCount - 1
                val nextInterval = if (newCount > 0) computeInterval() else 0L // Recalculer l'intervalle pour le nouveau compte, ou 0 si plus de cigarettes

                dataStore.saveStateWithTimestamp(nextInterval, newCount)

                // Si le compte est de nouveau 0, arrêter le timer. Sinon, redémarrer avec le nouvel intervalle.
                if (newCount == 0L) {
                    dataStore.setLastCigaretteTime(0L)
                    dataStore.setNextCigaretteTime(0L)
                    stopTimerService()
                } else {
                    val lastCigTime = dataStore.loadLastCigaretteTime() // On garde le dernier temps enregistré
                    dataStore.setNextCigaretteTime(lastCigTime + nextInterval)
                    startTimerService(nextInterval)
                }
                refresh()
            }
        }
    }

    private fun startTimerService(initialTime: Long) {
        val serviceIntent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_INITIAL_TIME, initialTime)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(serviceIntent)
        } else {
            getApplication<Application>().startService(serviceIntent)
        }
    }

    private fun stopTimerService() {
        val serviceIntent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        getApplication<Application>().stopService(serviceIntent)
    }

    // Nouvelle alarme pour la prochaine cigarette
    private fun scheduleNextCigaretteAlarm(timeInMillis: Long) {
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