package com.example.data

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class PrayerTimesResponse(
    val code: Int,
    val status: String,
    val data: PrayerTimesData?
)

@JsonClass(generateAdapter = true)
data class PrayerTimesData(
    val timings: Timings,
    val date: DateInfo? = null
)

@JsonClass(generateAdapter = true)
data class DateInfo(
    val readable: String,
    val hijri: HijriDate? = null
)

@JsonClass(generateAdapter = true)
data class HijriDate(
    val date: String,
    val month: HijriMonth? = null,
    val year: String,
    val day: String
)

@JsonClass(generateAdapter = true)
data class HijriMonth(
    val en: String,
    val ar: String
)

@JsonClass(generateAdapter = true)
data class Timings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String,
    val Sunrise: String
)

interface AladhanApi {
    @GET("timings")
    suspend fun getPrayerTimes(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2 // Islamic Society of North America
    ): PrayerTimesResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://api.aladhan.com/v1/"

    val api: AladhanApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(AladhanApi::class.java)
    }
}
