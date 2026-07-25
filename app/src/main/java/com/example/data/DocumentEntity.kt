package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val uploadedAt: Long,
    val docType: String = "ID Verification",
    val status: String = "PENDING"
)
