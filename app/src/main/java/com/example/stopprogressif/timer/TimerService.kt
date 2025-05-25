package com.example.stopprogressif.timer // <-- Correction du package

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.stopprogressif.R // Importe R pour accéder aux ressources comme l'icône
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint // Ajout de l'annotation Hilt
class TimerService : Service() {

    @Inject
    lateinit var timerController: TimerController // Hilt injectera l'instance du TimerController

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val CHANNEL_ID = "stop_progressif_timer_channel"
    private val NOTIF_ID = 1

    companion object {
        const val ACTION_START = "com.example.stopprogressif.timer.ACTION_START"
        const val ACTION_STOP = "com.example.stopprogressif.timer.ACTION_STOP"
        const val EXTRA_INITIAL_TIME = "initial_time"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService", "✅ Service démarré")

        createNotificationChannel()
        startForeground(NOTIF_ID, createNotification(0L)) // Notification initiale

        // Observe le temps restant du TimerController pour mettre à jour la notification
        serviceScope.launch {
            timerController.timeLeft.collect { timeLeft ->
                updateNotification(timeLeft)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialTime = intent?.getLongExtra(EXTRA_INITIAL_TIME, 0L) ?: 0L
        val action = intent?.action

        when (action) {
            ACTION_START -> {
                if (initialTime > 0) {
                    timerController.start(initialTime)
                    Log.d("TimerService", "⚙️ Commande reçue : START avec $initialTime ms")
                } else {
                    timerController.resume() // Si pas de temps initial, tenter de reprendre
                    Log.d("TimerService", "⚙️ Commande reçue : RESUME")
                }
            }
            ACTION_STOP -> {
                timerController.stop()
                Log.d("TimerService", "⚙️ Commande reçue : STOP")
                stopSelf()
            }
            else -> {
                Log.d("TimerService", "⚙️ Commande reçue : aucune action spécifiée")
            }
        }

        return START_STICKY // Relancé automatiquement si tué par Android
    }

    override fun onDestroy() {
        Log.d("TimerService", "🛑 Service arrêté")
        serviceScope.cancel()
        timerController.stop() // Assurez-vous d'arrêter le timer aussi
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Crée la notification affichant le temps restant
    private fun createNotification(timeRemaining: Long): Notification {
        val minutes = timeRemaining / 1000 / 60
        val seconds = (timeRemaining / 1000) % 60
        val contentText = String.format("Temps restant: %02d:%02d", minutes, seconds)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Minuteur actif")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher) // Utilisez votre icône d'application
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(timeRemaining: Long) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID, createNotification(timeRemaining))
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
}