package com.example.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Firestore Database Schema for User Profile
 * This demonstrates a secure, privacy-first data partitioning approach.
 */
@IgnoreExtraProperties
data class PublicProfile(
    var uid: String = "",
    var fullName: String = "",
    var country: String = "",
    var membershipStatus: String = "PENDING", 
    var isVerified: Boolean = false,
    var community: String = "",
    var expiryDate: String = "",
    var updatedAt: Long = System.currentTimeMillis()
)

/**
 * Sensitive identity information stored in a restricted subcollection
 * Collection Name: "users/{uid}/private_profile"
 * Document ID: "identity"
 */
@IgnoreExtraProperties
data class PrivateIdentity(
    var dob: String = "",
    var residency: String = "",
    var passportNumber: String = "",
    var licenseNumber: String = "",
    var updatedAt: Long = System.currentTimeMillis()
)
