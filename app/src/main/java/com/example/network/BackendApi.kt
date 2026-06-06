package com.example.network

import com.squareup.moshi.JsonClass
import retrofit2.http.POST
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class RegistrationRequest(
    val eventId: Int,
    val memberId: String,
    val memberEmail: String
)

interface BackendApi {
    @POST("v1/events/{eventId}/register")
    suspend fun registerForEvent(
        @Path("eventId") eventId: Int,
        @retrofit2.http.Body request: RegistrationRequest
    )
}
