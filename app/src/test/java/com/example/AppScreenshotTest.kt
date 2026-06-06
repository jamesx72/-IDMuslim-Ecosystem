package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.IDMuslimApp
import com.example.ui.theme.IDMuslimTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AppScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun app_screenshot() {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    com.example.utils.FirebaseHelper.initialize(context)
    com.example.network.ApiClient.initialize(context)
    com.example.network.EmailService.initialize(com.example.network.ApiClient.getSessionManager())

    composeTestRule.setContent { IDMuslimTheme { IDMuslimApp() } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/app.png")
  }
}
