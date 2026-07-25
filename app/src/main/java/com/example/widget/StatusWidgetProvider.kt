package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class StatusWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_status)
    
    val prefs = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
    val legacyVerified = prefs.getBoolean("is_verified", false)
    val defaultStatus = if (legacyVerified) "VERIFIED" else "UNVERIFIED"
    val status = prefs.getString("KEY_VERIFICATION_STATUS", defaultStatus) ?: defaultStatus
    
    val statusText = when (status) {
        "VERIFIED" -> "Vérifié"
        "PENDING" -> "En attente"
        else -> "Non Vérifié"
    }
    
    val color = when (status) {
        "VERIFIED" -> android.graphics.Color.parseColor("#81C784") // Green
        "PENDING" -> android.graphics.Color.parseColor("#FFD54F") // Amber
        else -> android.graphics.Color.parseColor("#E57373") // Red
    }
    
    views.setTextViewText(R.id.widget_status, statusText)
    views.setTextColor(R.id.widget_status, color)
    
    val intent = Intent(context, MainActivity::class.java).apply {
        action = "OPEN_DIGITAL_ID"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    views.setOnClickPendingIntent(R.id.widget_button, pendingIntent)
    
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
