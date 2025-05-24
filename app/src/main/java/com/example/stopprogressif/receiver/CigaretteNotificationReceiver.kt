
package com.example.stopprogressif.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.stopprogressif.NotificationHelper

class CigaretteNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val helper = NotificationHelper(context)
        helper.sendCigaretteReadyNotification()
    }
}


