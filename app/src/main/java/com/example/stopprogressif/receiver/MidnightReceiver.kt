package com.example.stopprogressif.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.stopprogressif.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.stopprogressif.data.DataStoreManager


class MidnightReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val appContext = context.applicationContext
        val dataStore = DataStoreManager(appContext)
        val notificationHelper = NotificationHelper(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            val prefs = appContext.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
            val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            val lastShown = prefs.getLong("notif_last_day", -1L)

            // Correction ici : Utilisation de saveStateWithTimestamp pour réinitialiser l'état
            dataStore.saveStateWithTimestamp(0L, 0L, System.currentTimeMillis())

            if (lastShown != today) {
                prefs.edit().putLong("notif_last_day", today).apply()
                notificationHelper.sendDailyResetNotification()
            }
        }
    }
}