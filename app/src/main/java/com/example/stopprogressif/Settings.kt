package com.stopprogressif

/**
 * Données de configuration persistées.
 *
 * @property modeSevrage Peut valoir [MODE_OBJECTIF] ou [MODE_ESPACEMENT].
 */
data class Settings(
    val prixPaquet: Float = 10f,
    val cigarettesParPaquet: Int = 20,
    val cigarettesHabituelles: Int = 30,
    val objectifParJour: Int = 20,
    val modeSevrage: String = MODE_OBJECTIF,
    val espacementHeures: Int = 1,
    val espacementMinutes: Int = 0
) {
    companion object {
        const val MODE_OBJECTIF = "OBJECTIF"    // Limite de cigarettes par jour
        const val MODE_ESPACEMENT = "ESPACEMENT" // Délai minimum entre deux cigarettes
    }
}
