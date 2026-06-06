package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.network.ApiClient
import com.example.network.EmailService
import com.example.ui.IDMuslimApp
import com.example.ui.theme.IDMuslimTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize secure network stack, Firebase and middleware
    com.example.utils.FirebaseHelper.initialize(this)
    ApiClient.initialize(this)
    EmailService.initialize(ApiClient.getSessionManager())
    
    enableEdgeToEdge()
    setContent {
      IDMuslimTheme {
        IDMuslimApp()
      }
    }
  }
}
