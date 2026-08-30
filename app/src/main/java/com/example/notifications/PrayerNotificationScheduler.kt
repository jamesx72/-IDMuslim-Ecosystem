package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.Timings
import com.example.network.ApiClient
import java.util.Calendar

object PrayerNotificationScheduler {

    private const val TAG = "PrayerScheduler"

    fun schedulePrayerNotifications(context: Context, timings: Timings) {
        val sessionManager = ApiClient.getSessionManager()
        if (!sessionManager.getPrayerNotifications()) {
            cancelAllPrayerNotifications(context)
            return
        }

        val preReminderMinutes = sessionManager.getPrePrayerReminderMinutes()

        val prayers = listOf(
            "Fajr" to timings.Fajr,
            "Dhuhr" to timings.Dhuhr,
            "Asr" to timings.Asr,
            "Maghrib" to timings.Maghrib,
            "Isha" to timings.Isha
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        prayers.forEachIndexed { index, (name, timeStr) ->
            try {
                // Parse timeStr like "05:12" or "05:12 (CEST)"
                val cleanTime = timeStr.split(" ")[0].trim()
                val parts = cleanTime.split(":")
                if (parts.size >= 2) {
                    val hour = parts[0].toInt()
                    val minute = parts[1].toInt()

                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)

                        // If time passed today, schedule for tomorrow
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }

                    // 1. Exact Prayer Time (Adhan) Notification
                    val prayerIntent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                        putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_NAME, name)
                        putExtra(PrayerNotificationReceiver.EXTRA_IS_PRE_REMINDER, false)
                    }

                    val prayerPendingIntent = PendingIntent.getBroadcast(
                        context,
                        index + 100, // requestCode 100..104
                        prayerIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    scheduleAlarm(alarmManager, calendar.timeInMillis, prayerPendingIntent)

                    // 2. Pre-Prayer Reminder (e.g. 15 minutes before)
                    if (preReminderMinutes > 0) {
                        val preCal = (calendar.clone() as Calendar).apply {
                            add(Calendar.MINUTE, -preReminderMinutes)
                        }

                        if (preCal.timeInMillis > System.currentTimeMillis()) {
                            val preIntent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                                putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_NAME, name)
                                putExtra(PrayerNotificationReceiver.EXTRA_IS_PRE_REMINDER, true)
                                putExtra(PrayerNotificationReceiver.EXTRA_REMINDER_MINUTES, preReminderMinutes)
                            }

                            val prePendingIntent = PendingIntent.getBroadcast(
                                context,
                                index + 600, // requestCode 600..604
                                preIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            scheduleAlarm(alarmManager, preCal.timeInMillis, prePendingIntent)
                        }
                    }

                    Log.d(TAG, "Scheduled Adhan and reminders for $name at ${calendar.time}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling prayer $name: ${e.message}")
            }
        }
    }

    private fun scheduleAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAllPrayerNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (i in 0..4) {
            // Main prayer intent
            val intent = Intent(context, PrayerNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                i + 100,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }

            // Pre-prayer reminder intent
            val prePendingIntent = PendingIntent.getBroadcast(
                context,
                i + 600,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (prePendingIntent != null) {
                alarmManager.cancel(prePendingIntent)
                prePendingIntent.cancel()
            }
        }
    }
}
