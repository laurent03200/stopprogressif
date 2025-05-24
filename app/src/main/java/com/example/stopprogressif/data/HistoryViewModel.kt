package com.example.stopprogressif.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stopprogressif.model.DailyReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToLong // Ajouté pour s'assurer que roundToLong est disponible si besoin ici

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = DataStoreManager(application)

    private val _allReports = MutableStateFlow<List<DailyReport>>(emptyList())

    private val _dailyReports = MutableStateFlow<List<DailyReport>>(emptyList())
    val dailyReports: StateFlow<List<DailyReport>> = _dailyReports.asStateFlow()

    private val _weeklyReports = MutableStateFlow<List<DailyReport>>(emptyList())
    val weeklyReports: StateFlow<List<DailyReport>> = _weeklyReports.asStateFlow()

    private val _monthlyReports = MutableStateFlow<List<DailyReport>>(emptyList())
    val monthlyReports: StateFlow<List<DailyReport>> = _monthlyReports.asStateFlow()

    init {
        viewModelScope.launch {
            loadReports() // Ajout du chargement automatique ici
            _allReports.collect { rawReports ->
                _dailyReports.value = rawReports.sortedByDescending { it.getLocalDate() }
                _weeklyReports.value = aggregateReports(rawReports, "weekly")
                _monthlyReports.value = aggregateReports(rawReports, "monthly")
            }
        }
    }

    suspend fun loadReports() {
        val reports = dataStore.loadAllDailyReports()
        _allReports.value = reports
    }

    private fun aggregateReports(rawReports: List<DailyReport>, periodType: String): List<DailyReport> {
        if (rawReports.isEmpty()) return emptyList()

        val sortedReports = rawReports.sortedBy { it.getLocalDate() }

        val aggregatedMap = mutableMapOf<Any, DailyReport.Builder>()

        sortedReports.forEach { report ->
            val date = report.getLocalDate()
            val key: Any = when (periodType) {
                "weekly" -> {
                    val weekFields = WeekFields.of(Locale.getDefault())
                    val weekOfYear = date.get(weekFields.weekOfWeekBasedYear())
                    val year = date.year
                    "$year-$weekOfYear"
                }
                "monthly" -> {
                    date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
                }
                else -> throw IllegalArgumentException("Type de période non supporté: $periodType")
            }

            val builder = aggregatedMap.getOrPut(key) {
                DailyReport.Builder(
                    date = date,
                    type = periodType
                )
            }

            builder.cigarettesSmoked += report.cigarettesSmoked
            builder.moneySavedCents += report.moneySavedCents
            builder.sumAvgIntervalMs += report.avgIntervalMs
            builder.sumAvgExceededMs += report.avgTimeExceededMs
            builder.countReports++
        }

        return aggregatedMap.values
            .map { it.build() }
            .sortedByDescending { it.getLocalDate() }
    }
}