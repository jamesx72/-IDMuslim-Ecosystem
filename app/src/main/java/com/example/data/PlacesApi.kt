package com.example.data

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class SearchNearbyRequest(
    val includedTypes: List<String>,
    val maxResultCount: Int,
    val locationRestriction: LocationRestriction
)

@JsonClass(generateAdapter = true)
data class LocationRestriction(
    val circle: Circle
)

@JsonClass(generateAdapter = true)
data class Circle(
    val center: LatLng,
    val radius: Double
)

@JsonClass(generateAdapter = true)
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

@JsonClass(generateAdapter = true)
data class SearchNearbyResponse(
    val places: List<Place>? = null
)

@JsonClass(generateAdapter = true)
data class Place(
    val displayName: DisplayName? = null,
    val formattedAddress: String? = null,
    val location: LatLng? = null
)

@JsonClass(generateAdapter = true)
data class DisplayName(
    val text: String,
    val languageCode: String? = null
)

interface PlacesApi {
    @POST("places:searchNearby")
    suspend fun searchNearby(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String = "places.displayName,places.formattedAddress,places.location",
        @Body request: SearchNearbyRequest
    ): SearchNearbyResponse
}

object PlacesApiClient {
    private const val BASE_URL = "https://places.googleapis.com/v1/"

    val api: PlacesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PlacesApi::class.java)
    }
}
