package com.example

import com.example.utils.VerificationPortalHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun verificationPortal_generatesValidSignedUrl() {
    val portal = VerificationPortalHelper.generatePortalUrl(
      memberId = "IDM-99881",
      fullName = "Tariq Ramadan",
      verificationStatus = "VERIFIED_LEVEL_3",
      community = "Mosquée de Lyon",
      durationSeconds = 600L
    )

    assertTrue("URL should start with verification portal endpoint", portal.url.startsWith("https://verify.idmuslim.org/portal?token="))
    assertNotNull("Token must not be null", portal.token)
    assertTrue("Signature should be 32 hex characters", portal.signature.length == 32)
    assertEquals("IDM-99881", portal.payload.memberId)
    assertEquals("Tariq Ramadan", portal.payload.fullName)

    // Test parsing and verification
    val parsed = VerificationPortalHelper.parseVerificationUrl(portal.url)
    assertNotNull("Parsed result should not be null", parsed)
    assertTrue("Parsed token must be valid", parsed!!.isValid)
    assertFalse("Token should not be expired yet", parsed.isExpired)
    assertEquals("IDM-99881", parsed.memberId)
    assertEquals("Tariq Ramadan", parsed.fullName)
  }
}
