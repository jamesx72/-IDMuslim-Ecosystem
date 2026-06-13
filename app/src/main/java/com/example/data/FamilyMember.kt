package com.example.data

data class FamilyMember(
    val id: String = "",
    val fullName: String = "",
    val dateOfBirth: String = "",
    val relation: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
