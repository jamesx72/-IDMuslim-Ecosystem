package com.example.data.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_identity_documents ORDER BY isFavorite DESC, updatedAt DESC")
    fun observeAllDocuments(): Flow<List<WalletDocumentEntity>>

    @Query("SELECT * FROM wallet_identity_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): WalletDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(document: WalletDocumentEntity)

    @Query("UPDATE wallet_identity_documents SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDocumentFavorite(id: String, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM wallet_identity_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    @Query("DELETE FROM wallet_identity_documents")
    suspend fun clearAllDocuments()

    @Query("SELECT * FROM wallet_sensitive_info ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAllSensitiveInfo(): Flow<List<SensitiveInfoEntity>>

    @Query("SELECT * FROM wallet_sensitive_info WHERE id = :id LIMIT 1")
    suspend fun getSensitiveInfoById(id: String): SensitiveInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSensitiveInfo(item: SensitiveInfoEntity)

    @Query("UPDATE wallet_sensitive_info SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSensitivePinned(id: String, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM wallet_sensitive_info WHERE id = :id")
    suspend fun deleteSensitiveInfoById(id: String)

    @Query("DELETE FROM wallet_sensitive_info")
    suspend fun clearAllSensitiveInfo()

    @Query("SELECT * FROM wallet_encrypted_files ORDER BY updatedAt DESC")
    fun observeAllEncryptedFiles(): Flow<List<EncryptedVaultFileEntity>>

    @Query("SELECT * FROM wallet_encrypted_files WHERE id = :id LIMIT 1")
    suspend fun getEncryptedFileById(id: String): EncryptedVaultFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEncryptedFile(file: EncryptedVaultFileEntity)

    @Query("UPDATE wallet_encrypted_files SET syncStatus = :status, firebaseDownloadUrl = :downloadUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEncryptedFileSyncStatus(
        id: String,
        status: String,
        downloadUrl: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM wallet_encrypted_files WHERE id = :id")
    suspend fun deleteEncryptedFileById(id: String)

    @Query("DELETE FROM wallet_encrypted_files")
    suspend fun clearAllEncryptedFiles()
}
