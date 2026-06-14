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
        private const val KEY_PROFILE_COMMUNITY_AFFILIATION = "profile_community_affiliation"
        private const val KEY_PASSPORT_NUMBER = "passport_number"
        private const val KEY_LICENSE_NUMBER = "license_number"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_HAS_PAID_PDF = "has_paid_pdf"
        private const val KEY_ID_READY_DISMISSED = "id_ready_dismissed"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        if (token.startsWith("demo_token_")) {
            saveUserEmail(token.substringAfter("demo_token_"))
        } else if (token.startsWith("google_demo_token_") && token != "google_demo_token_simulated") {
            saveUserEmail(token.substringAfter("google_demo_token_"))
        }
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String {
        val email = prefs.getString(KEY_USER_EMAIL, null)
        if (!email.isNullOrBlank()) return email
        
        val token = getAuthToken() ?: ""
        if (token.startsWith("google_demo_token_") && token != "google_demo_token_simulated") {
            return token.substringAfter("google_demo_token_")
        }
        if (token.startsWith("demo_token_")) {
            return token.substringAfter("demo_token_")
        }
        return "ouattarajamesx@gmail.com"
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

    fun saveProfileCommunityAffiliation(community: String) {
        prefs.edit().putString(KEY_PROFILE_COMMUNITY_AFFILIATION, community).apply()
    }

    fun getProfileCommunityAffiliation(): String? {
        return prefs.getString(KEY_PROFILE_COMMUNITY_AFFILIATION, null)
    }

    fun savePassportNumber(passportNumber: String) {
        prefs.edit().putString(KEY_PASSPORT_NUMBER, passportNumber).apply()
    }

    fun getPassportNumber(): String? {
        return prefs.getString(KEY_PASSPORT_NUMBER, null)
    }

    fun saveLicenseNumber(licenseNumber: String) {
        prefs.edit().putString(KEY_LICENSE_NUMBER, licenseNumber).apply()
    }

    fun getLicenseNumber(): String? {
        return prefs.getString(KEY_LICENSE_NUMBER, null)
    }

    fun saveHasPaidForPdf(hasPaid: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_PAID_PDF, hasPaid).apply()
    }

    fun hasPaidForPdf(): Boolean {
        return prefs.getBoolean(KEY_HAS_PAID_PDF, false)
    }

    fun saveIdReadyAlertDismissed(dismissed: Boolean) {
        prefs.edit().putBoolean(KEY_ID_READY_DISMISSED, dismissed).apply()
    }

    fun isIdReadyAlertDismissed(): Boolean {
        return prefs.getBoolean(KEY_ID_READY_DISMISSED, false)
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }

    fun saveLocalCredential(email: String, word: String, fullName: String) {
        val normalizedEmail = email.trim().lowercase()
        prefs.edit()
            .putString("LOCAL_PWD_$normalizedEmail", word)
            .putString("LOCAL_NAME_$normalizedEmail", fullName)
            .apply()
    }

    fun verifyLocalCredential(email: String, word: String): Boolean {
        val normalizedEmail = email.trim().lowercase()
        val stored = prefs.getString("LOCAL_PWD_$normalizedEmail", null)
        return stored != null && stored == word
    }

    fun getLocalUserFullName(email: String): String? {
        val normalizedEmail = email.trim().lowercase()
        return prefs.getString("LOCAL_NAME_$normalizedEmail", null)
    }

    fun savePrivacyMode(enabled: Boolean) {
        prefs.edit().putBoolean("KEY_PRIVACY_MODE", enabled).apply()
    }

    fun getPrivacyMode(): Boolean {
        return prefs.getBoolean("KEY_PRIVACY_MODE", false)
    }

    fun saveDarkTheme(theme: String) {
        prefs.edit().putString(KEY_DARK_THEME, theme).apply()
    }

    fun getDarkTheme(): String {
        return prefs.getString(KEY_DARK_THEME, "system") ?: "system"
    }
}
