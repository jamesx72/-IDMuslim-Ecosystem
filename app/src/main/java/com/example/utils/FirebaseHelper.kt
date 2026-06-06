package com.example.utils

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseHelper {
    fun initialize(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                // Programmatic initialization avoids demanding a statically compiled google-services.json
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDummyKeyForLocalCompilationAndInitialization")
                    .setApplicationId("1:123456789012:android:abcdef12345678")
                    .setProjectId("idmuslim-app")
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
