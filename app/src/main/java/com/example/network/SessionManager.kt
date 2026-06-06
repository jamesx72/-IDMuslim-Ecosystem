package com.example.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages the user's session, authentication token, and verification status.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_IS_VERIFIED = "is_verified"
        private const val KEY_PROFILE_PHOTO = "profile_photo"
        private const val KEY_CARD_THEME = "card_theme"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun setVerifiedStatus(isVerified: Boolean) {
        prefs.edit().putBoolean(KEY_IS_VERIFIED, isVerified).apply()
    }

    fun isUserVerified(): Boolean {
        // Default to false for the identity verification workflow
        return prefs.getBoolean(KEY_IS_VERIFIED, false)
    }

    fun saveProfilePhotoBase64(base64: String) {
        prefs.edit().putString(KEY_PROFILE_PHOTO, base64).apply()
    }

    fun getProfilePhotoBase64(): String? {
        return prefs.getString(KEY_PROFILE_PHOTO, null)
    }

    fun saveCardTheme(themeIndex: Int) {
        prefs.edit().putInt(KEY_CARD_THEME, themeIndex).apply()
    }

    fun getCardTheme(): Int {
        return prefs.getInt(KEY_CARD_THEME, 0)
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
}
