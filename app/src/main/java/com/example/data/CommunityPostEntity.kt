package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val type: String, // e.g. "UPDATE", "EVENT", "DONATION"
    val timestamp: Long,
    val authorName: String,
    val communityName: String
)
