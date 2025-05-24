package com.example.stopprogressif

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import com.example.stopprogressif.timer.TimerController

class TimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService", "✅ Service démarré")

                createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TimerService", "⚙️ Commande reçue : lancement du timer")

        serviceScope.launch {
            try {
                TimerController.resume() // ou start(...) si nécessaire
            } catch (e: Exception) {
                Log.e("TimerService", "❌ Erreur dans TimerLogicManager", e)
                stopSelf()
            }
        }

        return START_STICKY // Relancé automatiquement si tué par Android
    }

    override fun onDestroy() {
        Log.d("TimerService", "🛑 Service arrêté")
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Réduction progressive en cours")
            .setContentText("L'application suit vos objectifs.")
            .setSmallIcon(R.mipmap.ic_launcher) // Assure-toi que l’icône existe
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Suivi StopProgressif",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal pour les notifications de timer actif"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "stop_progressif_timer_channel"
        private const val NOTIF_ID = 1
    }
}
