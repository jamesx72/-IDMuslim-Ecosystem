package com.example.network

import com.squareup.moshi.Json
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Response models
data class PlacesAutocompleteResponse(
    val status: String,
    val predictions: List<PlacePrediction>?
)

data class PlacePrediction(
    val description: String,
    @Json(name = "place_id") val placeId: String?
)

interface PlacesApi {
    @GET("maps/api/place/autocomplete/json")
    suspend fun getPlacePredictions(
        @Query("input") input: String,
        @Query("key") apiKey: String,
        @Query("types") types: String = "mosque|hindu_temple|church|synagogue"
    ): PlacesAutocompleteResponse
}

object PlacesApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(MoshiConverterFactory.create(com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()))
        .build()

    val api: PlacesApi by lazy {
        retrofit.create(PlacesApi::class.java)
    }
}
