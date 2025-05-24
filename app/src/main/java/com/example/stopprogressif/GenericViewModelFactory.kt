package com.example.stopprogressif.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Fabrique générique de ViewModel pour simplifier l'injection.
 * Utilisable pour n'importe quel ViewModel via une lambda constructeur.
 */
class GenericViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {
    override fun <T1 : ViewModel> create(modelClass: Class<T1>): T1 {
        return creator() as T1
    }
}
