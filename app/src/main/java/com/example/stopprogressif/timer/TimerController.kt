package com.example.stopprogressif.timer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TimerController {

    enum class TimerState {
        IDLE, RUNNING, PAUSED, FINISHED
    }

    private var countdownJob: Job? = null
    private var totalTime: Long = 0L
    private var remainingTime: Long = 0L
    private var tickInterval: Long = 1000L // default 1s

    private val _state = MutableStateFlow(TimerState.IDLE)
    val state: StateFlow<TimerState> get() = _state

    private val _timeLeft = MutableStateFlow(0L)
    val timeLeft: StateFlow<Long> get() = _timeLeft

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(durationMillis: Long) {
        stop()
        totalTime = durationMillis
        remainingTime = durationMillis
        _timeLeft.value = remainingTime
        _state.value = TimerState.RUNNING
        countdownJob = scope.launch {
            while (remainingTime > 0) {
                delay(tickInterval)
                remainingTime -= tickInterval
                _timeLeft.value = remainingTime
            }
            _state.value = TimerState.FINISHED
        }
    }

    fun pause() {
        if (_state.value == TimerState.RUNNING) {
            countdownJob?.cancel()
            _state.value = TimerState.PAUSED
        }
    }

    fun resume() {
        if (_state.value == TimerState.PAUSED) {
            _state.value = TimerState.RUNNING
            countdownJob = scope.launch {
                while (remainingTime > 0) {
                    delay(tickInterval)
                    remainingTime -= tickInterval
                    _timeLeft.value = remainingTime
                }
                _state.value = TimerState.FINISHED
            }
        }
    }

    fun stop() {
        countdownJob?.cancel()
        countdownJob = null
        _state.value = TimerState.IDLE
        remainingTime = 0L
        _timeLeft.value = 0L
    }

    fun isRunning(): Boolean = _state.value == TimerState.RUNNING
}
