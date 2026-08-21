package com.example.data

data class UserDto(
    val uid: String = "",
    val fullName: String = "",
    val isVerified: Boolean = false,
    val dob: String = "",
    val residency: String = "",
    val community: String = "",
    val expiryDate: String = "",
    val country: String = "",
    val membershipStatus: String = "PENDING",
    val idNumber: String = "",
    val isSuspended: Boolean = false
)
