package com.example.data

sealed class SyncConflictType {
    data class ProfileConflict(
        val localProfile: UserProfileEntity,
        val cloudProfile: UserProfileEntity,
        val differences: List<String>
    ) : SyncConflictType()

    data class DocumentConflict(
        val localDocsCount: Int,
        val cloudDocsCount: Int,
        val differences: List<String>
    ) : SyncConflictType()
}

data class SyncConflict(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: SyncConflictType,
    val timestamp: Long = System.currentTimeMillis()
)
