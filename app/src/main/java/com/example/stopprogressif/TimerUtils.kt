package com.stopprogressif

import android.content.Context
import androidx.core.content.edit

/**
 * Utilitaires de persistance pour le minuteur.
 *
 * — Stocke le temps restant dans des SharedPreferences isolées.
 * — Utilise `applicationContext` pour éviter toute fuite mémoire.
 * — Expose aussi une méthode `clearRemainingTime` pratique pour les tests /
 *    la remise à zéro.
 */
object TimerUtils {

    private const val PREFS_NAME = "TimerPrefs"
    private const val KEY_REMAINING_TIME = "remainingTime"

    /** Sauvegarde de façon asynchrone le temps restant (en millisecondes). */
    fun saveRemainingTime(context: Context, remainingTime: Long) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit(commit = false) { putLong(KEY_REMAINING_TIME, remainingTime) }
    }

    /**
     * Charge le temps restant.
     * @return `null` si aucune valeur n’a encore été enregistrée.
     */
    fun loadRemainingTime(context: Context): Long? =
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .run {
                if (contains(KEY_REMAINING_TIME)) getLong(KEY_REMAINING_TIME, 0L) else null
            }

    /** Supprime la valeur stockée. Utile pour un reset ou des tests. */
    fun clearRemainingTime(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit(commit = false) { remove(KEY_REMAINING_TIME) }
    }
}
