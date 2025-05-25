package com.example.stopprogressif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(private val context: Context) {

    private val channelId = "cigarette_notification_channel"
    private val notificationId = 1001

    init {
        createNotificationChannel()
    }

    fun sendCigaretteReadyNotification() {
        sendNotification("Cigarette autorisée", "C'est le moment de fumer ta prochaine cigarette.")
    }

    fun sendMotivationNotification() {
        sendNotification("Reste motivé !", "Tu tiens bon, continue sur cette lancée.")
    }

    fun sendDailyResetNotification() {
        sendNotification("Nouvelle journée", "Le compteur est réinitialisé. Nouveau départ aujourd’hui.")
    }

    fun sendTimerFinishedNotification(lastUpdateTimeMillis: Long) {
        val formattedTime = DateTimeFormatter.ofPattern("HH:mm")
            .format(Instant.ofEpochMilli(lastUpdateTimeMillis).atZone(ZoneId.systemDefault()))
        sendNotification("Timer terminé", "Tu peux fumer une cigarette maintenant. Le temps s'est écoulé à $formattedTime.")
    }

    fun sendTimerElapsedNotification() {
        sendNotification("Temps écoulé !", "Le temps alloué est dépassé. Prenez conscience de votre dépassement.")
    }


    private fun sendNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Utilise une icône système par défaut
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifications Stop Progressif"
            val descriptionText = "Notifications liées à votre progression anti-tabac"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 300, 200)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}