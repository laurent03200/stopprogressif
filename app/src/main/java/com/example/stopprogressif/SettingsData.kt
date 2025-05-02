package com.stopprogressif

/**
 * Données de configuration persistées pour l'application StopProgressif.
 *
 * @property prixPaquet               Prix d’un paquet en euros.
 * @property cigarettesParPaquet      Nombre de cigarettes dans un paquet.
 * @property mode                     MODE_OBJECTIF ou MODE_INTERVALLE.
 *
 * @property objectifParJour          Objectif quotidien (mode OBJECTIF).
 * @property heuresDebut              Heure de début de la plage active (0‑23).
 * @property minutesDebut             Minute de début de la plage active (0‑59).
 * @property heuresFin                Heure de fin de la plage active (0‑23).
 * @property minutesFin               Minute de fin de la plage active (0‑59).
 *
 * @property heuresEntreCigarettes    Intervalle heures (mode INTERVALLE).
 * @property minutesEntreCigarettes   Intervalle minutes (mode INTERVALLE).
 *
 * @property cigarettesHabituelles    Consommation habituelle pour calcul d’économies.
 */

data class SettingsData(
    val prixPaquet: Float = 10f,
    val cigarettesParPaquet: Int = 20,
    val mode: String = MODE_OBJECTIF,

    // Mode Objectif
    val objectifParJour: Int = 20,
    val heuresDebut: Int = 7,
    val minutesDebut: Int = 0,
    val heuresFin: Int = 23,
    val minutesFin: Int = 0,

    // Mode Intervalle
    val heuresEntreCigarettes: Int = 1,
    val minutesEntreCigarettes: Int = 0,

    // Estimations
    val cigarettesHabituelles: Int = 30
) {
    companion object {
        const val MODE_OBJECTIF = "objectif"
        const val MODE_INTERVALLE = "intervalle"
    }
}
