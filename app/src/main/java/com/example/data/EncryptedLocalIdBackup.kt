package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EncryptedLocalIdBackup(
    @Json(name = "backupId") val backupId: String = "",
    @Json(name = "appName") val appName: String = "IDMuslim Digital ID",
    @Json(name = "backupType") val backupType: String = "SECURE_LOCAL_ENCRYPTED_JSON",
    @Json(name = "formatVersion") val formatVersion: String = "2.1",
    @Json(name = "generatedTimestamp") val generatedTimestamp: Long = System.currentTimeMillis(),
    @Json(name = "generatedDate") val generatedDate: String = "",
    @Json(name = "encryptionAlgorithm") val encryptionAlgorithm: String = "AES-256-GCM (Android KeyStore)",
    @Json(name = "integrityChecksumSha256") val integrityChecksumSha256: String = "",
    @Json(name = "recordsCount") val recordsCount: Int = 0,
    @Json(name = "publicSummary") val publicSummary: LocalBackupPublicSummary? = null,
    @Json(name = "encryptedIdentityPayload") val encryptedIdentityPayload: String = ""
)

@JsonClass(generateAdapter = true)
data class LocalBackupPublicSummary(
    @Json(name = "memberId") val memberId: String = "",
    @Json(name = "fullName") val fullName: String = "",
    @Json(name = "membershipTier") val membershipTier: String = "",
    @Json(name = "verificationStatus") val verificationStatus: String = "",
    @Json(name = "isVerified") val isVerified: Boolean = false,
    @Json(name = "issueDate") val issueDate: String = "",
    @Json(name = "expiryDate") val expiryDate: String = ""
)

@JsonClass(generateAdapter = true)
data class DecryptedIdentityPayload(
    @Json(name = "memberId") val memberId: String = "",
    @Json(name = "fullName") val fullName: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "birthDate") val birthDate: String = "",
    @Json(name = "bloodType") val bloodType: String = "",
    @Json(name = "city") val city: String = "",
    @Json(name = "emergencyContact") val emergencyContact: String = "",
    @Json(name = "avatarUrl") val avatarUrl: String = "",
    @Json(name = "isVerified") val isVerified: Boolean = false,
    @Json(name = "verificationStatus") val verificationStatus: String = "",
    @Json(name = "membershipTier") val membershipTier: String = "",
    @Json(name = "issueDate") val issueDate: String = "",
    @Json(name = "expiryDate") val expiryDate: String = "",
    @Json(name = "nfcSerial") val nfcSerial: String = "",
    @Json(name = "identityHash") val identityHash: String = "",
    @Json(name = "publicKey") val publicKey: String = "",
    @Json(name = "userProfile") val userProfile: UserProfileEntity? = null,
    @Json(name = "documents") val documents: List<DocumentEntity> = emptyList(),
    @Json(name = "securityLogs") val securityLogs: List<String> = emptyList(),
    @Json(name = "privacySettings") val privacySettings: Map<String, Boolean> = emptyMap()
)
