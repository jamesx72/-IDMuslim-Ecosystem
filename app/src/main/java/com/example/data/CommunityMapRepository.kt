package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.*

class CommunityMapRepository {

    companion object {
        // Default hub (Paris centre)
        const val DEFAULT_LAT = 48.8566
        const val DEFAULT_LNG = 2.3522

        fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadius = 6371000.0 // meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadius * c
        }

        fun formatDistance(meters: Double): String {
            return if (meters < 1000) {
                "${meters.roundToInt()} m"
            } else {
                val km = meters / 1000.0
                String.format(java.util.Locale.US, "%.1f km", km)
            }
        }
    }

    /**
     * Observes the cached places from the local Room database, ensuring full offline-first access.
     */
    fun getCachedPlacesFlow(cachedPlaceDao: CachedPlaceDao): Flow<List<CommunityPlace>> {
        return cachedPlaceDao.getAllCachedPlaces().map { entities ->
            entities.map { it.toCommunityPlace() }
        }
    }

    /**
     * Seeds or updates local Room cache with default locations, nearby dynamic points, and DB events.
     */
    suspend fun syncPlacesWithCache(
        cachedPlaceDao: CachedPlaceDao,
        userLat: Double,
        userLng: Double,
        databaseEvents: List<EventEntity>
    ) {
        val generatedPlaces = getPlaces(userLat, userLng, databaseEvents)
        val entities = generatedPlaces.map { CachedPlaceEntity.fromCommunityPlace(it) }
        cachedPlaceDao.insertAll(entities)
    }

    /**
     * Ensures an event is stored in the offline-first places cache as a community event marker.
     */
    suspend fun cacheEventAsPlace(
        cachedPlaceDao: CachedPlaceDao,
        event: EventEntity,
        userLat: Double = DEFAULT_LAT,
        userLng: Double = DEFAULT_LNG
    ) {
        val eventPlace = CommunityPlace(
            id = "event_${event.id}",
            name = event.title,
            type = CommunityPlaceType.COMMUNITY_EVENT,
            latitude = userLat + 0.0025,
            longitude = userLng - 0.0018,
            address = event.location.ifBlank { "Centre Communautaire IDMuslim" },
            description = event.description.ifBlank { "Événement communautaire organisé par ${event.organizer}" },
            openingHours = "${event.date} à ${event.time}",
            phone = null,
            rating = 5.0f,
            reviewCount = event.maxTickets.coerceAtLeast(10),
            services = listOf(
                "Organisé par ${event.organizer}",
                if (event.price == 0.0) "Entrée Gratuite" else "Tarif: ${event.price} €",
                "${event.availableTickets} places restantes"
            ),
            eventId = event.id,
            isCertified = true
        )
        cachedPlaceDao.insertPlace(CachedPlaceEntity.fromCommunityPlace(eventPlace))
    }

    fun getPlaces(userLat: Double, userLng: Double, databaseEvents: List<EventEntity>): List<CommunityPlace> {
        val result = mutableListOf<CommunityPlace>()

        // 1. Primary Curated Places in Paris & Major Centers
        val basePlaces = listOf(
            CommunityPlace(
                id = "mosque_gmp",
                name = "Grande Mosquée de Paris",
                type = CommunityPlaceType.MOSQUE,
                latitude = 48.8420,
                longitude = 2.3550,
                address = "2bis Place du Puits de l'Ermite, 75005 Paris",
                description = "Mosquée historique avec minaret de 33m, patio andalou, bibliothèque et salon de thé traditionnel.",
                openingHours = "05:00 - 23:00 • 5 Prières quotidiennes & Jumu'ah",
                phone = "+33 1 45 35 97 33",
                rating = 4.9f,
                reviewCount = 1420,
                services = listOf("Jumu'ah (2 services)", "Salle femmes", "Ablutions", "Cours d'Arabe", "Visites culturelles"),
                isCertified = true
            ),
            CommunityPlace(
                id = "mosque_omar",
                name = "Mosquée Omar Ibn Al-Khattab",
                type = CommunityPlaceType.MOSQUE,
                latitude = 48.8688,
                longitude = 2.3705,
                address = "4 Rue Morand, 75011 Paris",
                description = "Centre islamique actif proposant prières quotidiennes, conférences du vendredi et distribution solidaire.",
                openingHours = "05:30 - 22:30 • Ouvert tous les jours",
                phone = "+33 1 43 55 58 12",
                rating = 4.8f,
                reviewCount = 530,
                services = listOf("Jumu'ah", "Espace d'études", "Ablutions", "Librairie islamique"),
                isCertified = true
            ),
            CommunityPlace(
                id = "mosque_creteil",
                name = "Mosquée Sahaba de Créteil",
                type = CommunityPlaceType.MOSQUE,
                latitude = 48.7770,
                longitude = 2.4540,
                address = "4 Rue Jean Gabin, 94000 Créteil",
                description = "Grand complexe cultuel et culturel moderne avec coupole lumineuse et espaces pédagogiques.",
                openingHours = "05:00 - 23:00",
                phone = "+33 1 48 99 22 22",
                rating = 4.9f,
                reviewCount = 890,
                services = listOf("Jumu'ah", "Grand parking", "Salle polyvalente", "Ablutions modernes"),
                isCertified = true
            ),
            CommunityPlace(
                id = "halal_palmier",
                name = "Restaurant Le Palmier Gourmand",
                type = CommunityPlaceType.HALAL_RESTAURANT,
                latitude = 48.8525,
                longitude = 2.3680,
                address = "18 Rue de la Roquette, 75011 Paris",
                description = "Gastronomie orientale et grillades au feu de bois 100% Halal certifié, sans alcool, cadre familial.",
                openingHours = "11:30 - 23:30 • 7j/7",
                phone = "+33 1 48 06 14 20",
                rating = 4.8f,
                reviewCount = 380,
                services = listOf("Certifié AVS", "Sans alcool", "Espace prière", "À emporter & Livraison", "Climatisé"),
                isCertified = true,
                halalCertifier = "AVS & IDMuslim Shield"
            ),
            CommunityPlace(
                id = "halal_boucherie_baraka",
                name = "Boucherie & Épicerie Fine Al-Baraka",
                type = CommunityPlaceType.HALAL_MARKET,
                latitude = 48.8610,
                longitude = 2.3780,
                address = "52 Boulevard Voltaire, 75011 Paris",
                description = "Viandes françaises de premier choix, charcuterie artisanale halal et produits bio méditerranéens.",
                openingHours = "08:30 - 20:00 • Fermé lundi",
                phone = "+33 1 43 38 90 12",
                rating = 4.9f,
                reviewCount = 265,
                services = listOf("100% Halal Contrôlé", "Viande Bio", "Traiteur", "Paiement sans contact"),
                isCertified = true,
                halalCertifier = "Achahada & IDMuslim Shield"
            ),
            CommunityPlace(
                id = "halal_saveurs_orient",
                name = "Saveurs d'Orient & Grill",
                type = CommunityPlaceType.HALAL_RESTAURANT,
                latitude = 48.8475,
                longitude = 2.3410,
                address = "12 Rue Saint-Séverin, 75005 Paris",
                description = "Spécialités libanaises et méditerranéennes faites maison avec ingrédients frais et certification halal rigoureuse.",
                openingHours = "12:00 - 00:00",
                phone = "+33 1 43 26 88 90",
                rating = 4.7f,
                reviewCount = 410,
                services = listOf("Certifié Halal", "Options végétariennes", "Terrasse", "Service continu"),
                isCertified = true,
                halalCertifier = "IDMuslim Shield Certified"
            ),
            CommunityPlace(
                id = "mosque_st_denis",
                name = "Grande Mosquée de Saint-Denis",
                type = CommunityPlaceType.MOSQUE,
                latitude = 48.9320,
                longitude = 2.3580,
                address = "18 Boulevard Félix Faure, 93200 Saint-Denis",
                description = "Lieu de culte majeur en Île-de-France avec grand dôme et accueil chaleureux de la communauté.",
                openingHours = "05:00 - 23:00",
                phone = "+33 1 48 20 15 30",
                rating = 4.8f,
                reviewCount = 740,
                services = listOf("Jumu'ah", "Salles spacieuses", "Cours du soir", "Accès PMR"),
                isCertified = true
            )
        )

        result.addAll(basePlaces)

        // 2. If user is significantly far from Paris default (> 50 km), generate contextual dynamic establishments around user's actual GPS
        val distToParis = calculateDistanceMeters(userLat, userLng, DEFAULT_LAT, DEFAULT_LNG)
        if (distToParis > 50_000) {
            val dynamicLocalPlaces = listOf(
                CommunityPlace(
                    id = "local_mosque_central",
                    name = "Mosquée Centrale & Centre Culturel",
                    type = CommunityPlaceType.MOSQUE,
                    latitude = userLat + 0.0042,
                    longitude = userLng + 0.0035,
                    address = "Avenue de la Paix, Quartier Central",
                    description = "Mosquée de proximité proposant les 5 prières quotidiennes, le Jumu'ah et des activités communautaires.",
                    openingHours = "05:00 - 22:30",
                    phone = "+33 9 70 44 20 00",
                    rating = 4.9f,
                    reviewCount = 190,
                    services = listOf("Jumu'ah", "Salle femmes", "Ablutions", "Parking"),
                    isCertified = true
                ),
                CommunityPlace(
                    id = "local_halal_grill",
                    name = "Le Palmier d'Or Halal",
                    type = CommunityPlaceType.HALAL_RESTAURANT,
                    latitude = userLat - 0.0031,
                    longitude = userLng + 0.0048,
                    address = "Rue du Commerce, Centre-Ville",
                    description = "Restaurant familial aux spécialités grillades et tajines, certifié 100% Halal sans alcool.",
                    openingHours = "11:30 - 23:00",
                    phone = "+33 9 70 44 20 01",
                    rating = 4.8f,
                    reviewCount = 145,
                    services = listOf("Certifié Halal", "Sans alcool", "À emporter", "Terrasse"),
                    isCertified = true,
                    halalCertifier = "IDMuslim Shield"
                ),
                CommunityPlace(
                    id = "local_halal_market",
                    name = "Boucherie & Marché Al-Madina",
                    type = CommunityPlaceType.HALAL_MARKET,
                    latitude = userLat + 0.0028,
                    longitude = userLng - 0.0052,
                    address = "Boulevard des Martyrs",
                    description = "Boucherie traditionnelle, produits frais du terroir et alimentation générale halal.",
                    openingHours = "08:30 - 19:30",
                    phone = "+33 9 70 44 20 02",
                    rating = 4.9f,
                    reviewCount = 98,
                    services = listOf("Viande de qualité supérieure", "Traiteur", "Certifié"),
                    isCertified = true,
                    halalCertifier = "AVS / IDMuslim"
                ),
                CommunityPlace(
                    id = "local_mosque_noor",
                    name = "Mosquée An-Noor",
                    type = CommunityPlaceType.MOSQUE,
                    latitude = userLat - 0.0055,
                    longitude = userLng - 0.0038,
                    address = "Allée des Jardins",
                    description = "Centre spirituel et éducatif, cours pour enfants et prières en congrégation.",
                    openingHours = "05:15 - 22:30",
                    phone = "+33 9 70 44 20 03",
                    rating = 4.7f,
                    reviewCount = 85,
                    services = listOf("Jumu'ah", "Cours de Coran", "Ablutions"),
                    isCertified = true
                )
            )
            result.addAll(dynamicLocalPlaces)
        }

        // 3. Map Real Database Events to the Interactive Map!
        databaseEvents.forEachIndexed { index, event ->
            // Distribute events realistically in the vicinity
            val latOffset = ((index % 3 - 1) * 0.0065) + 0.0020
            val lngOffset = (((index + 1) % 3 - 1) * 0.0060) - 0.0015
            val eventLat = if (distToParis <= 50_000) (DEFAULT_LAT + latOffset) else (userLat + latOffset)
            val eventLng = if (distToParis <= 50_000) (DEFAULT_LNG + lngOffset) else (userLng + lngOffset)

            result.add(
                CommunityPlace(
                    id = "event_${event.id}",
                    name = event.title,
                    type = CommunityPlaceType.COMMUNITY_EVENT,
                    latitude = eventLat,
                    longitude = eventLng,
                    address = event.location.ifBlank { "Centre Communautaire IDMuslim" },
                    description = event.description.ifBlank { "Événement communautaire organisé par ${event.organizer}" },
                    openingHours = "${event.date} à ${event.time}",
                    phone = null,
                    rating = 5.0f,
                    reviewCount = event.maxTickets.coerceAtLeast(10),
                    services = listOf(
                        "Organisé par ${event.organizer}",
                        if (event.price == 0.0) "Entrée Gratuite" else "Tarif: ${event.price} €",
                        "${event.availableTickets} places restantes"
                    ),
                    eventId = event.id,
                    isCertified = true
                )
            )
        }

        return result
    }
}
