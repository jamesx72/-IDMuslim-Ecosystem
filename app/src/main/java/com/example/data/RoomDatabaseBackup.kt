package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoomDatabaseBackup(
    @Json(name = "backupTimestamp") val backupTimestamp: Long = System.currentTimeMillis(),
    @Json(name = "uid") val uid: String = "",
    @Json(name = "userProfile") val userProfile: UserProfileEntity? = null,
    @Json(name = "documents") val documents: List<DocumentEntity> = emptyList(),
    @Json(name = "activityLogs") val activityLogs: List<ActivityLogEntity> = emptyList(),
    @Json(name = "communityPosts") val communityPosts: List<CommunityPostEntity> = emptyList()
)
