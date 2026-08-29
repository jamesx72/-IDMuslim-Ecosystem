package com.example.data

enum class CommunityPlaceType(val key: String, val iconEmoji: String) {
    MOSQUE("mosque", "🕌"),
    HALAL_RESTAURANT("halal_restaurant", "🍽️"),
    HALAL_MARKET("halal_market", "🥩"),
    COMMUNITY_EVENT("community_event", "📅")
}

data class CommunityPlace(
    val id: String,
    val name: String,
    val type: CommunityPlaceType,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val description: String,
    val openingHours: String? = null,
    val phone: String? = null,
    val rating: Float? = 4.8f,
    val reviewCount: Int? = 120,
    val services: List<String> = emptyList(),
    val eventId: Int? = null,
    val isCertified: Boolean = true,
    val halalCertifier: String? = null
)
