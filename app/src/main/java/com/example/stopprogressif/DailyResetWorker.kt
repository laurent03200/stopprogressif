package com.example.stopprogressif

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stopprogressif.data.DataStoreManager
import com.example.stopprogressif.model.DailyReport
import java.time.LocalDate

class DailyResetWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val dataStore = DataStoreManager(context)

        val date = LocalDate.now().toString()
        val historique = dataStore.loadAllDailyReports()

        // Step 1: ARCHIVE yesterday's data
        val (interval, count, timestamp) = dataStore.loadStateWithTimestamp() // Récupère le timestamp
        val moyenneDepassement = historique.find { it.date == date }?.avgTimeExceededMs ?: 0L
        val report = DailyReport(
            date = date,
            cigarettesSmoked = count.toInt(),
            avgIntervalMs = interval,
            avgTimeExceededMs = moyenneDepassement,
            moneySavedCents = 0L,
            type = "daily"
        )

        val alreadyExists = historique.any { it.date == date && it.type == "daily" }
        if (!alreadyExists) {
            dataStore.saveDailyReport(report)
            Log.d("DEBUG_PROG", "📦 Rapport archivé pour $date")
        } else {
            Log.d("DEBUG_PROG", "🛑 Rapport existant conservé pour $date")
        }

        // Step 2: RESET EVERYTHING
        val now = System.currentTimeMillis()
        dataStore.saveStateWithTimestamp(0L, 0L, now) // <-- Correction ici : ajout du timestamp
        dataStore.setLastCigaretteTime(now)
        dataStore.setNextCigaretteTime(now)

        // Sauvegarder un rapport vide pour aujourd'hui (sans écraser un existant avec données)
        val newDate = LocalDate.now().toString()
        val alreadySaved = historique.any { it.date == newDate && it.type == "daily" && it.avgTimeExceededMs > 0 }
        if (!alreadySaved) {
            val newReport = DailyReport(
                date = newDate,
                cigarettesSmoked = 0,
                avgIntervalMs = 0L,
                avgTimeExceededMs = 0L,
                moneySavedCents = 0L,
                type = "daily"
            )
            dataStore.saveDailyReport(newReport)
            Log.d("DEBUG_PROG", "🔁 Nouveau rapport vide sauvegardé pour $newDate")
        }

        return Result.success()
    }
}