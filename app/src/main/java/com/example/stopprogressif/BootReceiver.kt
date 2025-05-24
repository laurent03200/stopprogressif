package com.example.stopprogressif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

                        // 🔁 2. Replanifier le reset quotidien à minuit
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val delay = Duration.between(now, nextMidnight)

            val resetWorker = OneTimeWorkRequestBuilder<DailyResetWorker>()
                .setInitialDelay(delay)
                .build()

            WorkManager.getInstance(context).enqueue(resetWorker)
        }
    }
}
