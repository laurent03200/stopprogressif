package com.example.stopprogressif.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stopprogressif.NotificationHelper

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val helper = NotificationHelper(applicationContext)
        helper.sendCigaretteReadyNotification()
        return Result.success()
    }
}
