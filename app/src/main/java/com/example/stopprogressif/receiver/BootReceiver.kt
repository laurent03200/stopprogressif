package com.example.stopprogressif.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build // Import added
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stopprogressif.data.DataStoreManager
import com.example.stopprogressif.DailyResetWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.max // Import added
import com.example.stopprogressif.TimerService // Importation ajoutée

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onReceive(context: Context, intent: Intent) {
        // Supprimé Intent.ACTION_QUICKBOOT_POWERON car non universellement supporté
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootReceiver", "Boot ou mise à jour de l'application détectée. Action: ${intent.action}")

            CoroutineScope(Dispatchers.IO).launch {
                // 🔁 1. Replanifier le reset quotidien à minuit
                val now = LocalDateTime.now()
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
                val delay = Duration.between(now, nextMidnight)

                val resetWorker = OneTimeWorkRequestBuilder<DailyResetWorker>()
                    .setInitialDelay(delay)
                    .build()

                WorkManager.getInstance(context).enqueue(resetWorker)
                Log.d("BootReceiver", "DailyResetWorker replanifié après le boot.")

                // 🔄 2. Relancer le service de timer si nécessaire
                val lastCigTime = dataStoreManager.loadLastCigaretteTime()
                val nextCigTime = dataStoreManager.loadNextCigaretteTime()

                if (lastCigTime != null && nextCigTime != null) {
                    val serviceIntent = Intent(context, TimerService::class.java).apply { // Utilisation de TimerService
                        action = TimerService.ACTION_START // Utilisation de TimerService.ACTION_START
                        val remaining = nextCigTime - System.currentTimeMillis()
                        putExtra(TimerService.EXTRA_INITIAL_TIME, max(0L, remaining)) // Utilisation de TimerService.EXTRA_INITIAL_TIME
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d("BootReceiver", "Tentative de relancer TimerService après le boot.")
                } else {
                    Log.d("BootReceiver", "Pas de données de dernière cigarette ou prochaine cigarette, TimerService non relancé.")
                }
            }
        }
    }
}