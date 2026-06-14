package com.example.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Firestore Database Schema for User Profile
 * Collection Name: "users"
 * Document ID: Firebase Auth UID
 */
@IgnoreExtraProperties
data class UserProfile(
    var uid: String = "",
    var fullName: String = "",
    var country: String = "",
    var membershipStatus: String = "PENDING", // e.g., PENDING, ACTIVE, EXPIRED, REVOKED
    var isVerified: Boolean = false,
    var dob: String = "",
    var residency: String = "",
    var community: String = "",
    var expiryDate: String = "",
    var updatedAt: Long = System.currentTimeMillis()
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "fullName" to fullName,
            "country" to country,
            "membershipStatus" to membershipStatus,
            "isVerified" to isVerified,
            "dob" to dob,
            "residency" to residency,
            "community" to community,
            "expiryDate" to expiryDate,
            "updatedAt" to updatedAt
        )
    }
}
