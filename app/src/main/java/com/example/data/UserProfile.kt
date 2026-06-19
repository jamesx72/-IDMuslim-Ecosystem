package com.example.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Clean Architecture Firestore Data Models for Secure User Profile Management.
 * Storing user profiles to store personal identification details and community metadata securely
 * using the Principle of Least Privilege: partitioning non-sensitive social/communal metrics
 * from government-issued identification numbers and secure privileges.
 *
 * Firebase Security Rules Proposal:
 * ```
 * service cloud.firestore {
 *   match /databases/{database}/documents {
 *     match /users/{userId} {
 *       // Anyone authenticated can read user public profiles
 *       allow read: if request.auth != null;
 *       // Only the authorized user can update their public profile fields
 *       allow write: if request.auth != null && request.auth.uid == userId;
 *
 *       match /private_profile/identity {
 *         // Secure: Only the absolute owner can read/write their sensitive identity fields
 *         allow read, write: if request.auth != null && request.auth.uid == userId;
 *       }
 *     }
 *   }
 * }
 * ```
 */

@IgnoreExtraProperties
data class PublicProfile(
    @DocumentId var uid: String = "",
    var fullName: String = "",
    var avatarUrl: String? = null,
    var country: String = "",
    var membershipStatus: String = "PENDING", // PENDING, ACTIVE, INACTIVE, SUSPENDED
    var isVerified: Boolean = false,
    var community: String = "",
    var expiryDate: String = "", // Profile or membership expiry
    var updatedAt: Long = System.currentTimeMillis(),
    
    // Local Community Metadata Model
    var communityMetadata: CommunityMetadata = CommunityMetadata()
) {
    /**
     * Checks if membership is verified, active, and fully community-approved
     */
    @Exclude
    fun isActiveCommunityMember(): Boolean {
        return isVerified && membershipStatus == "ACTIVE" && communityMetadata.isActive
    }
}

/**
 * Represents secure, verified metadata about the user's local community affiliation.
 */
@IgnoreExtraProperties
data class CommunityMetadata(
    var role: String = "MEMBER", // ADMIN, MODERATOR, MEMBER, GUEST
    var joinedAt: Long = System.currentTimeMillis(),
    var verificationLevel: Int = 1, // 0 = unverified, 1 = intermediate, 2 = high-tier eID verified
    var communityPoints: Int = 0,
    var badgeIds: List<String> = emptyList(),
    var isActive: Boolean = true,
    var department: String = "General"
)

/**
 * Highly Restricted Personal Identification Details.
 * Collection Path: /users/{uid}/private_profile/identity
 * This data class models highly sensitive identification attributes.
 */
@IgnoreExtraProperties
data class PrivateIdentity(
    var dob: String = "",
    var residency: String = "",
    var passportNumber: String = "",
    var licenseNumber: String = "",
    var docType: String = "", // e.g. Passport, National Identity Card, Driver License
    var docNumber: String = "", // Actual document identification number
    var issuingCountry: String = "",
    var expiryDate: String = "", // Document specific expiration date
    var ssnOrTaxIdHash: String? = null, // Secure SHA-256 hash representation of SSN/Tax or National Registry Key
    var updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Sanitizes or masks sensitive identification fields for safety prior to internal UI rendering.
     */
    @Exclude
    fun getMaskedPassportNumber(): String {
        if (passportNumber.length <= 4) return "****"
        return passportNumber.take(2) + "*".repeat(passportNumber.length - 4) + passportNumber.takeLast(2)
    }

    @Exclude
    fun getMaskedLicenseNumber(): String {
        if (licenseNumber.length <= 4) return "****"
        return licenseNumber.take(2) + "*".repeat(licenseNumber.length - 4) + licenseNumber.takeLast(2)
    }

    @Exclude
    fun getMaskedDocNumber(): String {
        if (docNumber.length <= 4) return "****"
        return docNumber.take(2) + "*".repeat(docNumber.length - 4) + docNumber.takeLast(2)
    }
}
