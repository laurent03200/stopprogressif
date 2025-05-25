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
            // Gérer le cas où le format des parties est incorrect ou incomplet
            return if (parts.size >= 6) {
                DailyReport(
                    date = parts[0],
                    cigarettesSmoked = parts[1].toIntOrNull() ?: 0,
                    avgTimeExceededMs = parts[2].toLongOrNull() ?: 0L,
                    avgIntervalMs = parts[3].toLongOrNull() ?: 0L,
                    moneySavedCents = parts[4].toLongOrNull() ?: 0L,
                    type = parts[5]
                )
            } else {
                // Retourner une valeur par défaut ou lancer une exception si le format est crucial
                // Pour l'instant, je retourne une valeur par défaut pour éviter les crashs.
                DailyReport(
                    date = LocalDate.now().toString(),
                    cigarettesSmoked = 0,
                    avgTimeExceededMs = 0L,
                    avgIntervalMs = 0L,
                    moneySavedCents = 0L,
                    type = "daily"
                )
            }
        }

        fun serializeList(reports: List<DailyReport>): String {
            return reports.joinToString("|") { it.serializeInternal() }
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
        var moneySavedCents: Long = 0L,
        var countReports: Int = 0
    ) {
        fun build(): DailyReport {
            return DailyReport(
                date = this.date.toString(),
                cigarettesSmoked = this.cigarettesSmoked,
                avgTimeExceededMs = if (countReports > 0) (sumAvgExceededMs.toDouble() / countReports).roundToLong() else 0L,
                avgIntervalMs = if (countReports > 0) (sumAvgIntervalMs.toDouble() / countReports).roundToLong() else 0L,
                moneySavedCents = this.moneySavedCents,
                type = this.type
            )
        }
    }
}