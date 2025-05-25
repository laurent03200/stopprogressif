package com.example.stopprogressif.timer

import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import android.os.Build // Added this import

@Singleton // Indique que Hilt doit fournir une seule instance de cette classe
class TimerController @Inject constructor(
    private val context: Context // Hilt fournira le Context
) {

    enum class TimerState {
        IDLE, RUNNING, PAUSED, FINISHED
    }

    private var countdownJob: Job? = null
    private var totalTime: Long = 0L
    private var remainingTime: Long = 0L
    private val tickInterval: Long = 1000L // default 1s

    private val _state = MutableStateFlow(TimerState.IDLE)
    val state: StateFlow<TimerState> get() = _state

    private val _timeLeft = MutableStateFlow(0L)
    val timeLeft: StateFlow<Long> get() = _timeLeft

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(durationMillis: Long) {
        stop() // Arrête tout timer précédent
        totalTime = durationMillis
        remainingTime = durationMillis
        _timeLeft.value = remainingTime
        _state.value = TimerState.RUNNING
        countdownJob = scope.launch {
            Log.d("TimerController", "Timer STARTED for $durationMillis ms")
            while (remainingTime > 0) {
                delay(tickInterval)
                remainingTime -= tickInterval
                _timeLeft.value = remainingTime
            }
            _state.value = TimerState.FINISHED
            Log.d("TimerController", "Timer FINISHED.")
            vibratePhone() // Vibrate when the timer finishes
            Log.d("TimerController", "Timer job CANCELLED (not finished naturally).") // This log seems misplaced, should be in stop()
        }
    }

    fun pause() {
        if (_state.value == TimerState.RUNNING) {
            countdownJob?.cancel()
            _state.value = TimerState.PAUSED
            Log.d("TimerController", "Timer PAUSED. Remaining: $remainingTime ms")
        }
    }

    fun resume() {
        if (_state.value == TimerState.PAUSED) {
            _state.value = TimerState.RUNNING
            Log.d("TimerController", "Timer RESUMED. Remaining: $remainingTime ms")
            // Relance le compte à rebours depuis le temps restant
            start(remainingTime) // Redémarre avec le temps restant comme nouvelle durée
        }
    }

    fun stop() {
        countdownJob?.cancel()
        countdownJob = null
        _state.value = TimerState.IDLE
        remainingTime = 0L
        _timeLeft.value = 0L
        Log.d("TimerController", "Timer STOPPED and reset.")
    }

    // Fonction pour faire vibrer le téléphone
    private fun vibratePhone() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
}