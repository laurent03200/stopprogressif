package com.example.stopprogressif.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.stopprogressif.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper // Injecte le NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarme reçue! Action: ${intent.action}")

        when (intent.action) {
            "com.example.stopprogressif.ACTION_TRIGGER_ALARM" -> {
                // Envoyer une notification quand l'alarme est déclenchée
                notificationHelper.sendCigaretteReadyNotification()
                Log.d("AlarmReceiver", "Notification 'Cigarette prête' envoyée.")

                // Optionnel: Vous pourriez vouloir démarrer ou redémarrer le TimerService ici
                // Ou déclencher une mise à jour dans le ViewModel via un intent implicite ou un WorkManager
            }
            // Ajoutez d'autres actions d'alarme si nécessaire
            else -> {
                Log.d("AlarmReceiver", "Action d'alarme inconnue: ${intent.action}")
            }
        }
    }
}