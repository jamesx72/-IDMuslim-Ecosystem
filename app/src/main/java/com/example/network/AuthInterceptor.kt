package com.example.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Authentication Middleware (Interceptor)
 * Ensures that only verified members can trigger sensitive actions.
 * Appends the authorization token to the request and checks verification status.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Enforce secure access: block sensitive requests if the user is not verified
        if (!sessionManager.isUserVerified()) {
            throw IOException("Access Denied: User is not verified. Cannot perform sensitive actions.")
        }

        val originalRequest = chain.request()
        
        // Append secure authentication headers (only for our own backend endpoints, skip for SendGrid)
        val token = sessionManager.getAuthToken()
        val securedRequest = if (token != null && !originalRequest.url.host.contains("sendgrid.com")) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(securedRequest)
    }
}
