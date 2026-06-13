package com.example.utils

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.example.BuildConfig

object FirebaseHelper {
    fun initialize(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                // Programmatic initialization avoids demanding a statically compiled google-services.json
                val apiKey = BuildConfig.FIREBASE_API_KEY
                val appId = BuildConfig.FIREBASE_APP_ID
                val projectId = BuildConfig.FIREBASE_PROJECT_ID

                val options = FirebaseOptions.Builder()
                    .setApiKey(if (apiKey.contains("YOUR_FIREBASE")) "AIzaSyDummyKeyForLocalCompilationAndInitialization" else apiKey)
                    .setApplicationId(if (appId.contains("YOUR_FIREBASE")) "1:123456789012:android:abcdef12345678" else appId)
                    .setProjectId(if (projectId.contains("YOUR_FIREBASE")) "idmuslim-app" else projectId)
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d("FirebaseHelper", "FirebaseApp initialized programmatically successfully!")
            } catch (e: Exception) {
                Log.e("FirebaseHelper", "Error initializing Firebase programmatically: ${e.message}")
            }
        } else {
            Log.d("FirebaseHelper", "FirebaseApp already initialized automatically.")
        }
    }
}
