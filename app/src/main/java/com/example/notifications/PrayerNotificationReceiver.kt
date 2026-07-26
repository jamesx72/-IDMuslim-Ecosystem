package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prière"
        showPrayerNotification(context, prayerName)
    }

    private fun showPrayerNotification(context: Context, prayerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "prayer_times_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels des Heures de Prière",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avis des heures de prière (Adhan)"
                enableVibration(true)
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("C'est l'heure de la prière de $prayerName !")
            .setContentText("Athan - Il est l'heure d'accomplir la prière de $prayerName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(getPrayerNotificationId(prayerName), notification)
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
    }
}
