package com.example.stopprogressif.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stopprogressif.data.CigaretteTrackerStoreManager
import com.example.stopprogressif.model.DailyReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CigaretteTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = CigaretteTrackerStoreManager(application)

    private val _lastCigaretteTime = MutableStateFlow<Long?>(null)
    val lastCigaretteTime: StateFlow<Long?> = _lastCigaretteTime.asStateFlow()

    private val _nextCigaretteTime = MutableStateFlow<Long?>(null)
    val nextCigaretteTime: StateFlow<Long?> = _nextCigaretteTime.asStateFlow()

    private val _allReports = MutableStateFlow<List<DailyReport>>(emptyList())
    val allReports: StateFlow<List<DailyReport>> = _allReports.asStateFlow()

    private val _cigarettesFumees = MutableStateFlow(0)
    val cigarettesFumees: StateFlow<Int> = _cigarettesFumees.asStateFlow()

    init {
        viewModelScope.launch {
            _lastCigaretteTime.value = dataStore.getLastCigaretteTime()
            val (delay, count, _) = dataStore.loadStateWithTimestamp()
            _nextCigaretteTime.value = _lastCigaretteTime.value?.plus(delay.toLong())
            _allReports.value = dataStore.loadAllDailyReports()
            _cigarettesFumees.value = count
        }
    }

    fun fumerUneCigarette() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            _lastCigaretteTime.value = now
            val (delay, count, _) = dataStore.loadStateWithTimestamp()
            _nextCigaretteTime.value = now + delay
            _cigarettesFumees.value = count + 1
            dataStore.saveLastCigaretteTime(now)
            dataStore.saveCigarettesFumees(count + 1)
        }
    }

    fun annulerDerniereCigarette() {
        viewModelScope.launch {
            val (delay, count, _) = dataStore.loadStateWithTimestamp()
            _cigarettesFumees.value = (count - 1).coerceAtLeast(0)
            dataStore.saveCigarettesFumees(_cigarettesFumees.value)
        }
    }

    fun getReportsForMonth(yearMonth: String): List<DailyReport> =
        _allReports.value.filter { it.date.startsWith(yearMonth) }

    fun getReportsForYear(year: String): List<DailyReport> =
        _allReports.value.filter { it.date.startsWith(year) }
}
