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
        private const val KEY_DOC_TYPE = "doc_type"
        private const val KEY_DOC_NUMBER = "doc_number"
        private const val KEY_ISSUING_COUNTRY = "issuing_country"
        private const val KEY_EXPIRY_DATE = "expiry_date"
        private const val KEY_BIOMETRIC_LOCK = "biometric_lock_enabled"
        private const val KEY_SCREEN_SECURITY = "screen_security_enabled"
        private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
        private const val KEY_SECURITY_AUDIT_LOGS = "security_audit_logs"
        private const val KEY_ACCOUNT_SUSPENDED = "account_suspended"
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

    fun savePrayerCalculationMethod(methodId: Int) {
        prefs.edit().putInt("KEY_PRAYER_CALCULATION_METHOD", methodId).apply()
    }

    fun getPrayerCalculationMethod(): Int {
        return prefs.getInt("KEY_PRAYER_CALCULATION_METHOD", 12) // Default to 12 (UOIF France)
    }

    fun saveCachedMosques(jsonString: String) {
        prefs.edit().putString("KEY_CACHED_MOSQUES_JSON", jsonString).apply()
    }

    fun getCachedMosques(): String? {
        return prefs.getString("KEY_CACHED_MOSQUES_JSON", null)
    }

    fun clearMosqueCache() {
        prefs.edit().remove("KEY_CACHED_MOSQUES_JSON").apply()
    }

    fun saveConfiguredMosque(name: String, address: String?) {
        prefs.edit()
            .putString("KEY_CONFIGURED_MOSQUE_NAME", name)
            .putString("KEY_CONFIGURED_MOSQUE_ADDRESS", address ?: "")
            .apply()
    }

    fun getConfiguredMosque(): Pair<String, String>? {
        val name = prefs.getString("KEY_CONFIGURED_MOSQUE_NAME", null) ?: return null
        val addr = prefs.getString("KEY_CONFIGURED_MOSQUE_ADDRESS", "") ?: ""
        return Pair(name, addr)
    }

    fun clearConfiguredMosque() {
        prefs.edit()
            .remove("KEY_CONFIGURED_MOSQUE_NAME")
            .remove("KEY_CONFIGURED_MOSQUE_ADDRESS")
            .apply()
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

    fun saveDocType(docType: String) {
        prefs.edit().putString(KEY_DOC_TYPE, docType).apply()
    }

    fun getDocType(): String? {
        return prefs.getString(KEY_DOC_TYPE, null)
    }

    fun saveDocNumber(docNumber: String) {
        prefs.edit().putString(KEY_DOC_NUMBER, docNumber).apply()
    }

    fun getDocNumber(): String? {
        return prefs.getString(KEY_DOC_NUMBER, null)
    }

    fun saveIssuingCountry(issuingCountry: String) {
        prefs.edit().putString(KEY_ISSUING_COUNTRY, issuingCountry).apply()
    }

    fun getIssuingCountry(): String? {
        return prefs.getString(KEY_ISSUING_COUNTRY, null)
    }

    fun saveExpiryDate(expiryDate: String) {
        prefs.edit().putString(KEY_EXPIRY_DATE, expiryDate).apply()
    }

    fun getExpiryDate(): String? {
        return prefs.getString(KEY_EXPIRY_DATE, null)
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
    
    fun saveProfileVisibility(visibility: String) {
        prefs.edit().putString("KEY_PROFILE_VISIBILITY", visibility).apply()
    }

    fun getProfileVisibility(): String {
        return prefs.getString("KEY_PROFILE_VISIBILITY", "Public") ?: "Public"
    }

    fun saveShowEmail(show: Boolean) {
        prefs.edit().putBoolean("KEY_SHOW_EMAIL", show).apply()
    }

    fun getShowEmail(): Boolean {
        return prefs.getBoolean("KEY_SHOW_EMAIL", false)
    }

    fun saveShareLocation(share: Boolean) {
        prefs.edit().putBoolean("KEY_SHARE_LOCATION", share).apply()
    }

    fun getShareLocation(): Boolean {
        return prefs.getBoolean("KEY_SHARE_LOCATION", true)
    }

    fun saveShareData(share: Boolean) {
        prefs.edit().putBoolean("KEY_SHARE_DATA", share).apply()
    }

    fun getShareData(): Boolean {
        return prefs.getBoolean("KEY_SHARE_DATA", false)
    }

    fun saveAllowNotifications(allow: Boolean) {
        prefs.edit().putBoolean("KEY_ALLOW_NOTIFICATIONS", allow).apply()
    }

    fun getAllowNotifications(): Boolean {
        return prefs.getBoolean("KEY_ALLOW_NOTIFICATIONS", true)
    }

    // Granular Shared Link Privacy Preferences
    fun saveShareLinkDob(share: Boolean) {
        prefs.edit().putBoolean("KEY_SHARE_LINK_DOB", share).apply()
    }

    fun getShareLinkDob(): Boolean {
        return prefs.getBoolean("KEY_SHARE_LINK_DOB", true)
    }

    fun saveShareLinkResidency(share: Boolean) {
        prefs.edit().putBoolean("KEY_SHARE_LINK_RESIDENCY", share).apply()
    }

    fun getShareLinkResidency(): Boolean {
        return prefs.getBoolean("KEY_SHARE_LINK_RESIDENCY", true)
    }

    fun saveShareLinkCommunity(share: Boolean) {
        prefs.edit().putBoolean("KEY_SHARE_LINK_COMMUNITY", share).apply()
    }

    fun getShareLinkCommunity(): Boolean {
        return prefs.getBoolean("KEY_SHARE_LINK_COMMUNITY", true)
    }

    fun saveShareLinkStatus(share: Boolean) {
        prefs.edit().putBoolean("KEY_SHARE_LINK_STATUS", share).apply()
    }

    fun getShareLinkStatus(): Boolean {
        return prefs.getBoolean("KEY_SHARE_LINK_STATUS", true)
    }

    fun saveShareLinkFullName(share: Boolean) {
        prefs.edit().putBoolean("KEY_SHARE_LINK_FULL_NAME", share).apply()
    }

    fun getShareLinkFullName(): Boolean {
        return prefs.getBoolean("KEY_SHARE_LINK_FULL_NAME", true)
    }

    fun saveShareLinkPhoto(share: Boolean) {
        prefs.edit().putBoolean("KEY_SHARE_LINK_PHOTO", share).apply()
    }

    fun getShareLinkPhoto(): Boolean {
        return prefs.getBoolean("KEY_SHARE_LINK_PHOTO", false)
    }

    fun setHasSeenTutorial(hasSeen: Boolean) {
        prefs.edit().putBoolean("KEY_HAS_SEEN_TUTORIAL", hasSeen).apply()
    }
    
    fun hasSeenTutorial(): Boolean {
        return prefs.getBoolean("KEY_HAS_SEEN_TUTORIAL", false)
    }

    fun saveBiometricLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()
        addSecurityAuditLog("Sécurité Biométrique", if (enabled) "Verrouillage biométrique activé" else "Verrouillage biométrique désactivé")
    }

    fun isBiometricLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_LOCK, false)
    }

    fun saveScreenSecurityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCREEN_SECURITY, enabled).apply()
        addSecurityAuditLog("Protection de l'Écran", if (enabled) "Capture d'écran bloquée pour la confidentialité" else "Capture d'écran autorisée")
    }

    fun isScreenSecurityEnabled(): Boolean {
        return prefs.getBoolean(KEY_SCREEN_SECURITY, false)
    }

    fun saveAutoLockTimeout(timeout: String) {
        prefs.edit().putString(KEY_AUTO_LOCK_TIMEOUT, timeout).apply()
        addSecurityAuditLog("Délai d'Auto-verrouillage", "Nouveau délai réglé sur $timeout")
    }

    fun getAutoLockTimeout(): String {
        return prefs.getString(KEY_AUTO_LOCK_TIMEOUT, "5 min") ?: "5 min"
    }

    fun saveLastSyncTime(timestamp: Long) {
        prefs.edit().putLong("KEY_LAST_SYNC_TIME", timestamp).apply()
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong("KEY_LAST_SYNC_TIME", 0L)
    }

    fun saveBackgroundSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("KEY_BACKGROUND_SYNC_ENABLED", enabled).apply()
    }

    fun isBackgroundSyncEnabled(): Boolean {
        return prefs.getBoolean("KEY_BACKGROUND_SYNC_ENABLED", true)
    }

    fun addSecurityAuditLog(category: String, detail: String) {
        val timestamp = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val newLog = "[$timestamp] $category: $detail"
        val currentLogs = getSecurityAuditLogs().toMutableList()
        currentLogs.add(0, newLog)
        val trimmed = currentLogs.take(20)
        prefs.edit().putStringSet(KEY_SECURITY_AUDIT_LOGS, trimmed.toSet()).apply()
    }

    fun getSecurityAuditLogs(): List<String> {
        val set = prefs.getStringSet(KEY_SECURITY_AUDIT_LOGS, emptySet()) ?: emptySet()
        return set.toList().sortedDescending()
    }

    fun saveAccountSuspended(suspended: Boolean) {
        prefs.edit().putBoolean(KEY_ACCOUNT_SUSPENDED, suspended).apply()
    }

    fun isAccountSuspended(): Boolean {
        return prefs.getBoolean(KEY_ACCOUNT_SUSPENDED, false)
    }

    fun saveLastLocalBackupTime(timestamp: Long) {
        prefs.edit().putLong("KEY_LAST_LOCAL_BACKUP_TIME", timestamp).apply()
    }

    fun getLastLocalBackupTime(): Long {
        return prefs.getLong("KEY_LAST_LOCAL_BACKUP_TIME", 0L)
    }

    fun saveAutoLocalBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("KEY_AUTO_LOCAL_BACKUP_ENABLED", enabled).apply()
    }

    fun isAutoLocalBackupEnabled(): Boolean {
        return prefs.getBoolean("KEY_AUTO_LOCAL_BACKUP_ENABLED", true)
    }

    fun saveLastLocalBackupPath(path: String) {
        prefs.edit().putString("KEY_LAST_LOCAL_BACKUP_PATH", path).apply()
    }

    fun getLastLocalBackupPath(): String? {
        return prefs.getString("KEY_LAST_LOCAL_BACKUP_PATH", null)
    }

    fun saveLastLocalBackupSize(size: Long) {
        prefs.edit().putLong("KEY_LAST_LOCAL_BACKUP_SIZE", size).apply()
    }

    fun getLastLocalBackupSize(): Long {
        return prefs.getLong("KEY_LAST_LOCAL_BACKUP_SIZE", 0L)
    }

    // Solar-Adaptive Theme Methods (Sunrise/Sunset automated switcher)
    fun saveSolarAdaptiveThemeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("KEY_SOLAR_ADAPTIVE_THEME_ENABLED", enabled).apply()
    }

    fun isSolarAdaptiveThemeEnabled(): Boolean {
        return prefs.getBoolean("KEY_SOLAR_ADAPTIVE_THEME_ENABLED", true)
    }

    fun saveSolarSimulationOverride(override: String) {
        prefs.edit().putString("KEY_SOLAR_SIMULATION_OVERRIDE", override).apply()
    }

    fun getSolarSimulationOverride(): String {
        return prefs.getString("KEY_SOLAR_SIMULATION_OVERRIDE", "AUTO") ?: "AUTO"
    }

    fun saveLastSolarLocation(lat: Double, lng: Double, city: String? = null) {
        prefs.edit()
            .putFloat("KEY_SOLAR_LAT", lat.toFloat())
            .putFloat("KEY_SOLAR_LNG", lng.toFloat())
            .putString("KEY_SOLAR_CITY", city ?: "")
            .apply()
    }

    fun getLastSolarLocation(): Triple<Double, Double, String?> {
        val lat = prefs.getFloat("KEY_SOLAR_LAT", 48.8566f).toDouble()
        val lng = prefs.getFloat("KEY_SOLAR_LNG", 2.3522f).toDouble()
        val city = prefs.getString("KEY_SOLAR_CITY", null)?.ifBlank { null }
        return Triple(lat, lng, city)
    }

    // Card Details Accessibility Font Scaling
    fun saveCardFontScale(scale: Float) {
        prefs.edit().putFloat("KEY_CARD_FONT_SCALE", scale).apply()
    }

    fun getCardFontScale(): Float {
        return prefs.getFloat("KEY_CARD_FONT_SCALE", 1.0f)
    }
}
