package com.example

import android.os.Bundle
import android.view.WindowManager
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
  private var lastInteractionTime: Long = System.currentTimeMillis()
  private var isBiometricPromptShowing = false
  private val TIMEOUT_MILLIS = 30 * 1000L

  @kotlin.OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

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
      val startRoute = if (intent?.action == "OPEN_DIGITAL_ID") "profile" else "auth"
      IDMuslimApp(startRoute)
    }
  }

  override fun onUserInteraction() {
    super.onUserInteraction()
    checkTimeoutAndAuthenticate()
    lastInteractionTime = System.currentTimeMillis()
  }

  override fun onResume() {
    super.onResume()
    checkTimeoutAndAuthenticate()
    lastInteractionTime = System.currentTimeMillis()
  }

  private fun checkTimeoutAndAuthenticate() {
    val sessionManager = ApiClient.getSessionManager()
    if (!isBiometricPromptShowing && sessionManager.getAuthToken() != null && sessionManager.isBiometricLockEnabled()) {
      val currentTime = System.currentTimeMillis()
      if (currentTime - lastInteractionTime > TIMEOUT_MILLIS) {
        triggerBiometricReAuth()
      }
    }
  }

  private fun triggerBiometricReAuth() {
    if (com.example.security.BiometricHelper.canAuthenticate(this)) {
      isBiometricPromptShowing = true
      com.example.security.BiometricHelper.authenticate(
        activity = this,
        title = "Session Verrouillée",
        subtitle = "Veuillez vous réauthentifier pour continuer",
        onSuccess = {
          isBiometricPromptShowing = false
          lastInteractionTime = System.currentTimeMillis()
        },
        onError = {
          isBiometricPromptShowing = false
          finishAffinity()
        }
      )
    }
  }
}
