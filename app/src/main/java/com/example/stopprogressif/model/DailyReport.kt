package com.example.stopprogressif.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.math.roundToLong // AJOUTEZ CETTE IMPORTATION

/**
 * Rapport quotidien/agrégé utilisé pour l’historique.
 *
 * @property date Date du rapport. Pour les quotidiens, c'est la date du jour.
 * Pour les agrégations (hebdo/mensuel), cela peut être la date de fin de la période.
 * @property cigarettesSmoked Nombre de cigarettes fumées sur la période. (Renommé de 'cigarettes')
 * @property avgTimeExceededMs Durée moyenne de dépassement du timer en millisecondes.
 * @property avgIntervalMs Durée moyenne des intervalles réels en millisecondes.
 * @property moneySavedCents Économies réalisées exprimées en centimes.
 * @property type Type de rapport: "daily", "weekly", ou "monthly". (Nouvelle propriété)
 */
@Serializable
data class DailyReport(
    val date: String,
    val cigarettesSmoked: Int,
    val avgTimeExceededMs: Long,
    val avgIntervalMs: Long,
    val moneySavedCents: Long,
    val type: String
) {
    // Méthode pour obtenir la date en tant que LocalDate
    fun getLocalDate(): LocalDate = LocalDate.parse(date)

    companion object {
        fun deserialize(serialized: String): DailyReport {
            val parts = serialized.split(";")
            if (parts.size < 6) {
                throw IllegalArgumentException("Format de DailyReport invalide: $serialized")
            }
            return DailyReport(
                date = parts[0],
                cigarettesSmoked = parts[1].toInt(),
                avgTimeExceededMs = parts[2].toLong(),
                avgIntervalMs = parts[3].toLong(),
                moneySavedCents = parts[4].toLong(),
                type = parts[5]
            )
        }

        fun serializeList(list: List<DailyReport>): String {
            return list.joinToString("|") { it.serializeInternal() }
        }

        fun deserializeList(serialized: String): List<DailyReport> {
            if (serialized.isBlank()) return emptyList()
            return serialized.split("|").map { deserialize(it) }
        }
    }

    private fun serializeInternal(): String {
        return listOf(
            date,
            cigarettesSmoked,
            avgTimeExceededMs,
            avgIntervalMs,
            moneySavedCents,
            type
        ).joinToString(";")
    }

    // Classe Builder imbriquée pour faciliter l'agrégation
    data class Builder(
        val date: LocalDate,
        val type: String,
        var cigarettesSmoked: Int = 0,
        var sumAvgIntervalMs: Long = 0L,
        var sumAvgExceededMs: Long = 0L,
        var moneySavedCents: Long = 0L, // AJOUTEZ 'var' ici pour la mutabilité
        var countReports: Int = 0
    ) {
        fun build(): DailyReport {
            return DailyReport(
                date = this.date.toString(),
                cigarettesSmoked = this.cigarettesSmoked,
                avgTimeExceededMs = if (countReports > 0) (sumAvgExceededMs.toDouble() / countReports).roundToLong() else 0L, // .toDouble() ajouté
                avgIntervalMs = if (countReports > 0) (sumAvgIntervalMs.toDouble() / countReports).roundToLong() else 0L, // .toDouble() ajouté
                moneySavedCents = this.moneySavedCents, // Cette ligne était la 87, elle est maintenant correcte
                type = this.type
            )
        }
    }
}