package com.example.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    private var retrofit: Retrofit? = null
    private var sessionManager: SessionManager? = null

    fun initialize(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context)
        }
        
        if (retrofit == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            // Registering our custom secure Authentication Middleware
            val authInterceptor = AuthInterceptor(sessionManager!!)

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()

            retrofit = Retrofit.Builder()
                // Use a placeholder backend URL
                .baseUrl("https://api.idmuslim.backend.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()))
                .build()
        }
    }

    val backendApi: BackendApi by lazy {
        retrofit?.create(BackendApi::class.java)
            ?: throw IllegalStateException("ApiClient not initialized. Call initialize(context) first.")
    }
    
    // Provide session manager for other local uses if needed
    fun getSessionManager(): SessionManager {
        return sessionManager ?: throw IllegalStateException("ApiClient not initialized.")
    }
}
