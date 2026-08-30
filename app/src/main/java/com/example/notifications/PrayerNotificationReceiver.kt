package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.network.ApiClient
import com.example.utils.AdhanAudioPlayer

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prière"
        val isPreReminder = intent.getBooleanExtra(EXTRA_IS_PRE_REMINDER, false)
        val reminderMinutes = intent.getIntExtra(EXTRA_REMINDER_MINUTES, 15)

        showPrayerNotification(context, prayerName, isPreReminder, reminderMinutes)
    }

    private fun showPrayerNotification(
        context: Context,
        prayerName: String,
        isPreReminder: Boolean,
        reminderMinutes: Int
    ) {
        val sessionManager = ApiClient.getSessionManager()
        val isAdhanAudioEnabled = sessionManager.isAdhanAudioEnabled()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = if (isPreReminder) "prayer_pre_reminder_channel" else "prayer_times_channel"
        val channelName = if (isPreReminder) "Rappels Pré-Prière" else "Avis Adhan Heures de Prière"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications et Adhan pour les 5 prières quotidiennes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPreReminder) {
            "⏰ Rappel Prière : $prayerName dans $reminderMinutes min"
        } else {
            "🕌 C'est l'heure de la prière de $prayerName !"
        }

        val text = if (isPreReminder) {
            "Préparez vos ablutions (Wudu). L'heure de $prayerName approche."
        } else {
            "Allahu Akbar — L'Adhan retentit pour la prière de $prayerName."
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))

        if (!isPreReminder && isAdhanAudioEnabled) {
            // Trigger audio playback for Adhan alert
            AdhanAudioPlayer.playAdhan(context)
        }

        val notificationId = if (isPreReminder) {
            getPrayerNotificationId(prayerName) + 500
        } else {
            getPrayerNotificationId(prayerName)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun getPrayerNotificationId(prayerName: String): Int {
        return when (prayerName) {
            "Fajr" -> 1001
            "Dhuhr" -> 1002
            "Asr" -> 1003
            "Maghrib" -> 1004
            "Isha" -> 1005
            else -> 1000
        }
    }

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_IS_PRE_REMINDER = "extra_is_pre_reminder"
        const val EXTRA_REMINDER_MINUTES = "extra_reminder_minutes"
    }
}
