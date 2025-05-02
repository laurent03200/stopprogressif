package com.stopprogressif

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Fabrique de [ProgressifViewModel].
 *
 * — Prend l’[Application] pour fournir le contexte à DataStoreManager.
 * — Utilise l’opérateur `when` pour un code plus clair et prépare le terrain
 *    si d’autres ViewModels devaient être ajoutés.
 */
class ProgressifViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(ProgressifViewModel::class.java) ->
            ProgressifViewModel(application) as T

        else -> throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
