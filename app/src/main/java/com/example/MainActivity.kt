package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.network.ApiClient
import com.example.network.EmailService
import com.example.ui.IDMuslimApp
import com.example.ui.theme.IDMuslimTheme

import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : FragmentActivity() {
  @kotlin.OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize secure network stack, Firebase and middleware
    com.example.utils.FirebaseHelper.initialize(this)
    ApiClient.initialize(this)
    EmailService.initialize(ApiClient.getSessionManager())
    
    enableEdgeToEdge()
    setContent {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(
          android.Manifest.permission.POST_NOTIFICATIONS
        )
        androidx.compose.runtime.LaunchedEffect(Unit) {
          if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
          }
        }
      }

      IDMuslimTheme {
        IDMuslimApp()
      }
    }
  }
}
