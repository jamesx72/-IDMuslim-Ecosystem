package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_places")
data class CachedPlaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val description: String,
    val openingHours: String? = null,
    val phone: String? = null,
    val rating: Float = 4.8f,
    val reviewCount: Int = 100,
    val servicesJson: String = "",
    val eventId: Int? = null,
    val isCertified: Boolean = true,
    val halalCertifier: String? = null,
    val lastCachedTimestamp: Long = System.currentTimeMillis()
) {
    fun toCommunityPlace(): CommunityPlace {
        val placeType = try {
            CommunityPlaceType.valueOf(type)
        } catch (e: Exception) {
            CommunityPlaceType.MOSQUE
        }

        val servicesList = if (servicesJson.isNotBlank()) {
            servicesJson.split(";;;").filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        return CommunityPlace(
            id = id,
            name = name,
            type = placeType,
            latitude = latitude,
            longitude = longitude,
            address = address,
            description = description,
            openingHours = openingHours,
            phone = phone,
            rating = rating,
            reviewCount = reviewCount,
            services = servicesList,
            eventId = eventId,
            isCertified = isCertified,
            halalCertifier = halalCertifier
        )
    }

    companion object {
        fun fromCommunityPlace(place: CommunityPlace): CachedPlaceEntity {
            return CachedPlaceEntity(
                id = place.id,
                name = place.name,
                type = place.type.name,
                latitude = place.latitude,
                longitude = place.longitude,
                address = place.address,
                description = place.description,
                openingHours = place.openingHours,
                phone = place.phone,
                rating = place.rating ?: 4.8f,
                reviewCount = place.reviewCount ?: 100,
                servicesJson = place.services.joinToString(";;;"),
                eventId = place.eventId,
                isCertified = place.isCertified,
                halalCertifier = place.halalCertifier,
                lastCachedTimestamp = System.currentTimeMillis()
            )
        }
    }
}
