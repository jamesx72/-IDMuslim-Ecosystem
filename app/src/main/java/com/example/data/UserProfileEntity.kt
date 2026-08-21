package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val uid: String,
    val fullName: String,
    val avatarUrl: String?,
    val membershipStatus: String,
    val isVerified: Boolean,
    val community: String,
    val expiryDate: String,
    
    // Private identity fields
    val dob: String,
    val residency: String,
    val passportNumber: String,
    val licenseNumber: String,
    val docType: String,
    val docNumber: String,
    val issuingCountry: String,
    
    val idNumber: String,
    
    val lastSyncTime: Long = System.currentTimeMillis()
)
