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
        private const val KEY_PROFILE_FULL_NAME = "profile_full_name"
        private const val KEY_PROFILE_DOB = "profile_dob"
        private const val KEY_PROFILE_RESIDENCY = "profile_residency"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun setVerifiedStatus(isVerified: Boolean) {
        prefs.edit().putBoolean(KEY_IS_VERIFIED, isVerified).apply()
        if (isVerified) {
            saveVerificationStatus("VERIFIED")
        } else {
            saveVerificationStatus("UNVERIFIED")
        }
    }

    fun isUserVerified(): Boolean {
        return getVerificationStatus() == "VERIFIED"
    }

    fun saveVerificationStatus(status: String) {
        prefs.edit().putString("KEY_VERIFICATION_STATUS", status).apply()
    }

    fun getVerificationStatus(): String {
        val legacyVerified = prefs.getBoolean(KEY_IS_VERIFIED, false)
        val defaultStatus = if (legacyVerified) "VERIFIED" else "UNVERIFIED"
        return prefs.getString("KEY_VERIFICATION_STATUS", defaultStatus) ?: defaultStatus
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

    fun saveLanguage(language: String) {
        prefs.edit().putString("KEY_LANGUAGE", language).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("KEY_LANGUAGE", "fr") ?: "fr"
    }

    fun savePrayerNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("KEY_PRAYER_NOTIFICATIONS", enabled).apply()
    }

    fun getPrayerNotifications(): Boolean {
        return prefs.getBoolean("KEY_PRAYER_NOTIFICATIONS", true)
    }

    fun saveProfileFullName(fullName: String) {
        prefs.edit().putString(KEY_PROFILE_FULL_NAME, fullName).apply()
    }

    fun getProfileFullName(): String? {
        return prefs.getString(KEY_PROFILE_FULL_NAME, null)
    }

    fun saveProfileDob(dob: String) {
        prefs.edit().putString(KEY_PROFILE_DOB, dob).apply()
    }

    fun getProfileDob(): String? {
        return prefs.getString(KEY_PROFILE_DOB, null)
    }

    fun saveProfileResidency(residency: String) {
        prefs.edit().putString(KEY_PROFILE_RESIDENCY, residency).apply()
    }

    fun getProfileResidency(): String? {
        return prefs.getString(KEY_PROFILE_RESIDENCY, null)
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
}
