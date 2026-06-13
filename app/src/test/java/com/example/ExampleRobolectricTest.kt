package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.IslamicDateHeader
import com.example.ui.theme.IDMuslimTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("IDMuslim", appName)
  }

  @Test
  fun `islamic date header displays and can open dialog`() {
    composeTestRule.setContent {
      IDMuslimTheme {
        IslamicDateHeader(language = "fr")
      }
    }

    // Verify card exists
    composeTestRule.onNodeWithTag("islamic_date_card").assertExists()

    // Perform tap/click to open calibration
    composeTestRule.onNodeWithTag("islamic_date_card").performClick()

    // Verify interactive calibration dialogue renders correctly
    composeTestRule.onNodeWithTag("islamic_date_dialog").assertExists()
  }
}
