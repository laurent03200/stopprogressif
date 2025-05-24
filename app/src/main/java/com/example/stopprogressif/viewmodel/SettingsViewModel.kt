package com.example.stopprogressif.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stopprogressif.data.DataStoreManager
import com.example.stopprogressif.data.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = DataStoreManager(application)

    private val _priceState = MutableStateFlow("10.0")
    val priceState: StateFlow<String> = _priceState

    private val _cigarettesPerPackState = MutableStateFlow("20")
    val cigarettesPerPackState: StateFlow<String> = _cigarettesPerPackState

    private val _cigarettesHabituellesState = MutableStateFlow("30")
    val cigarettesHabituellesState: StateFlow<String> = _cigarettesHabituellesState

    private val _hoursState = MutableStateFlow("1")
    val hoursState: StateFlow<String> = _hoursState

    private val _minutesState = MutableStateFlow("0")
    val minutesState: StateFlow<String> = _minutesState

    private val _errors = MutableStateFlow<Map<String, String?>>(emptyMap())
    val errors: StateFlow<Map<String, String?>> = _errors

    init {
        loadSettings()
    }

    fun updatePrice(value: String) { _priceState.value = value }
    fun updateCigarettesPerPack(value: String) { _cigarettesPerPackState.value = value }
    fun updateCigarettesHabituelles(value: String) { _cigarettesHabituellesState.value = value }
    fun updateHours(value: String) { _hoursState.value = value }
    fun updateMinutes(value: String) { _minutesState.value = value }

    fun saveSettings(): Boolean {
        val newErrors = mutableMapOf<String, String?>()
        val price = _priceState.value.toFloatOrNull()
        val pack = _cigarettesPerPackState.value.toIntOrNull()
        val hab = _cigarettesHabituellesState.value.toIntOrNull()
        val hrs = _hoursState.value.toIntOrNull()
        val min = _minutesState.value.toIntOrNull()

        if (price == null || price <= 0f) newErrors["price"] = "Prix invalide"
        if (pack == null || pack <= 0) newErrors["pack"] = "Cigarettes/paquet invalide"
        if (hab == null || hab <= 0) newErrors["habit"] = "Consommation habituelle invalide"
        if (hrs == null || hrs < 0) newErrors["hours"] = "Heures invalides"
        if (min == null || min < 0 || min > 59) newErrors["minutes"] = "Minutes invalides"

        _errors.value = newErrors
        if (newErrors.isNotEmpty()) return false

        viewModelScope.launch {
            val settings = Settings(
                prixPaquet = price!!,
                cigarettesParPaquet = pack!!,
                espacementHeures = hrs!!,
                espacementMinutes = min!!,
                cigarettesHabituelles = hab!!
            )
            dataStore.saveSettings(settings)
        }
        return true
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = dataStore.loadSettings()
            _priceState.value = settings.prixPaquet.toString()
            _cigarettesPerPackState.value = settings.cigarettesParPaquet.toString()
            _cigarettesHabituellesState.value = settings.cigarettesHabituelles.toString()
            _hoursState.value = settings.espacementHeures.toString()
            _minutesState.value = settings.espacementMinutes.toString()
        }
    }
}