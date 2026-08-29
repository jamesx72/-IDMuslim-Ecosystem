package com.example

import com.example.utils.VerificationPortalHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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

    assertTrue("URL should start with verification portal endpoint", portal.url.startsWith("https://verify.idmuslim.org/portal"))
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

  @Test
  fun cachedPlaceEntity_conversion_isAccurate() {
    val place = com.example.data.CommunityPlace(
      id = "test_mosque_1",
      name = "Mosquée Test Al-Falah",
      type = com.example.data.CommunityPlaceType.MOSQUE,
      latitude = 48.8566,
      longitude = 2.3522,
      address = "123 Rue de la République",
      description = "Mosquée communautaire pour prières et rencontres",
      openingHours = "05:00 - 23:00",
      phone = "+33 1 23 45 67 89",
      rating = 4.9f,
      reviewCount = 120,
      services = listOf("Jumu'ah", "Salle femmes", "Ablutions"),
      eventId = null,
      isCertified = true,
      halalCertifier = null
    )

    val entity = com.example.data.CachedPlaceEntity.fromCommunityPlace(place)
    assertEquals("test_mosque_1", entity.id)
    assertEquals("MOSQUE", entity.type)
    assertEquals(48.8566, entity.latitude, 0.0001)
    assertEquals(2.3522, entity.longitude, 0.0001)
    assertTrue(entity.servicesJson.contains("Jumu'ah"))

    val convertedBack = entity.toCommunityPlace()
    assertEquals(place.id, convertedBack.id)
    assertEquals(place.name, convertedBack.name)
    assertEquals(place.type, convertedBack.type)
    assertEquals(place.address, convertedBack.address)
    assertEquals(place.services.size, convertedBack.services.size)
    assertEquals(place.isCertified, convertedBack.isCertified)
  }

  @Test
  fun communityMapRepository_cachesEventsAndPlaces() {
    val repository = com.example.data.CommunityMapRepository()
    val testEvents = listOf(
      com.example.data.EventEntity(
        id = 42,
        title = "Conférence Annuelle Solidarité",
        description = "Grande conférence sur la solidarité et l'éducation",
        date = "2026-11-20",
        time = "19:00",
        location = "Centre Culturel Musulman",
        organizer = "IDMuslim Association",
        price = 0.0,
        maxTickets = 200,
        availableTickets = 150
      )
    )

    val places = repository.getPlaces(
      userLat = com.example.data.CommunityMapRepository.DEFAULT_LAT,
      userLng = com.example.data.CommunityMapRepository.DEFAULT_LNG,
      databaseEvents = testEvents
    )

    assertTrue("Places should contain curated mosques", places.any { it.type == com.example.data.CommunityPlaceType.MOSQUE })
    assertTrue("Places should contain halal spots", places.any { it.type == com.example.data.CommunityPlaceType.HALAL_RESTAURANT || it.type == com.example.data.CommunityPlaceType.HALAL_MARKET })
    
    val eventPlace = places.find { it.eventId == 42 }
    assertNotNull("Database event should be converted to a place marker", eventPlace)
    assertEquals("Conférence Annuelle Solidarité", eventPlace?.name)
    assertEquals(com.example.data.CommunityPlaceType.COMMUNITY_EVENT, eventPlace?.type)

    // Distance calculation test
    val dist = com.example.data.CommunityMapRepository.calculateDistanceMeters(48.8566, 2.3522, 48.8566, 2.3522)
    assertEquals(0.0, dist, 0.01)
    val distText = com.example.data.CommunityMapRepository.formatDistance(450.0)
    assertEquals("450 m", distText)
    val distKmText = com.example.data.CommunityMapRepository.formatDistance(3200.0)
    assertEquals("3.2 km", distKmText)
  }
}
