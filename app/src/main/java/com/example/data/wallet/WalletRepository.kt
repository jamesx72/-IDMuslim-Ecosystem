package com.example.data.wallet

import android.content.Context
import com.example.data.DocumentEntity
import com.example.data.UserProfileEntity
import com.example.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class WalletRepository(
    private val walletDao: WalletDao,
    private val cryptoManager: CryptoManager = CryptoManager.getInstance()
) {
    val allDocuments: Flow<List<WalletDocumentEntity>> =
        walletDao.observeAllDocuments().map { list ->
            list.map { it.decryptedFields(cryptoManager) }
        }

    val allSensitiveInfo: Flow<List<SensitiveInfoEntity>> =
        walletDao.observeAllSensitiveInfo().map { list ->
            list.map { it.decryptedFields(cryptoManager) }
        }

    val allEncryptedFiles: Flow<List<EncryptedVaultFileEntity>> =
        walletDao.observeAllEncryptedFiles().map { list ->
            list.map { it.decryptedFields(cryptoManager) }
        }

    suspend fun saveDocument(document: WalletDocumentEntity) = withContext(Dispatchers.IO) {
        val normalized = document.copy(updatedAt = System.currentTimeMillis())
        walletDao.upsertDocument(normalized.encryptedFields(cryptoManager))
    }

    suspend fun toggleDocumentFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        walletDao.updateDocumentFavorite(id, isFavorite, System.currentTimeMillis())
    }

    suspend fun deleteDocument(id: String) = withContext(Dispatchers.IO) {
        walletDao.deleteDocumentById(id)
    }

    suspend fun saveSensitiveInfo(item: SensitiveInfoEntity) = withContext(Dispatchers.IO) {
        val normalized = item.copy(updatedAt = System.currentTimeMillis())
        walletDao.upsertSensitiveInfo(normalized.encryptedFields(cryptoManager))
    }

    suspend fun toggleSensitivePinned(id: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        walletDao.updateSensitivePinned(id, isPinned, System.currentTimeMillis())
    }

    suspend fun deleteSensitiveInfo(id: String) = withContext(Dispatchers.IO) {
        walletDao.deleteSensitiveInfoById(id)
    }

    suspend fun uploadEncryptedUri(
        context: Context,
        uri: android.net.Uri,
        title: String,
        docCategory: String,
        uploadToCloud: Boolean = true
    ): Result<EncryptedVaultFileEntity> = com.example.security.EncryptedDocumentVaultManager.encryptAndStoreUri(
        context = context,
        uri = uri,
        customTitle = title,
        docCategory = docCategory,
        walletDao = walletDao,
        cryptoManager = cryptoManager,
        uploadToCloud = uploadToCloud
    )

    suspend fun uploadEncryptedBytes(
        context: Context,
        plainBytes: ByteArray,
        fileName: String,
        mimeType: String,
        title: String,
        docCategory: String,
        uploadToCloud: Boolean = true
    ): Result<EncryptedVaultFileEntity> = com.example.security.EncryptedDocumentVaultManager.encryptAndStoreBytes(
        context = context,
        plainBytes = plainBytes,
        fileName = fileName,
        mimeType = mimeType,
        customTitle = title,
        docCategory = docCategory,
        walletDao = walletDao,
        cryptoManager = cryptoManager,
        uploadToCloud = uploadToCloud
    )

    suspend fun decryptVaultFileBytes(
        context: Context,
        entity: EncryptedVaultFileEntity
    ): Result<ByteArray> = com.example.security.EncryptedDocumentVaultManager.decryptDocumentBytes(context, entity)

    suspend fun retryEncryptedFileCloudSync(
        entity: EncryptedVaultFileEntity
    ): Result<EncryptedVaultFileEntity> = com.example.security.EncryptedDocumentVaultManager.syncLocalEncryptedFileToCloud(
        entity = entity,
        walletDao = walletDao,
        cryptoManager = cryptoManager
    )

    suspend fun deleteEncryptedFile(entity: EncryptedVaultFileEntity) {
        com.example.security.EncryptedDocumentVaultManager.deleteEncryptedDocument(entity, walletDao)
    }

    suspend fun purgeEntireVault() = withContext(Dispatchers.IO) {
        walletDao.clearAllDocuments()
        walletDao.clearAllSensitiveInfo()
        walletDao.clearAllEncryptedFiles()
    }

    suspend fun loadTelemetry(context: Context): SqlCipherVaultTelemetry = withContext(Dispatchers.IO) {
        SecureWalletDatabase.inspectTelemetry(context)
    }

    suspend fun rotateMasterKey(context: Context): Boolean = withContext(Dispatchers.IO) {
        SecureWalletDatabase.rotateEncryptionKey(context)
    }

    /**
     * Imports real user-authored credentials from the existing IDMuslim profile & scanned documents
     * without duplicating already-imported records. Returns count of newly imported items.
     */
    suspend fun importFromProfileAndScannedDocs(
        profile: UserProfileEntity?,
        fallbackFullName: String,
        fallbackPassport: String,
        fallbackLicense: String,
        fallbackDocType: String,
        fallbackDocNumber: String,
        fallbackCountry: String,
        fallbackExpiry: String,
        scannedDocs: List<DocumentEntity>
    ): Int = withContext(Dispatchers.IO) {
        val existingDocs = allDocuments.first()
        val existingNumbers = existingDocs.map { it.documentNumber.trim().lowercase() }.toSet()
        var importedCount = 0

        val holder = profile?.fullName?.takeIf { it.isNotBlank() } ?: fallbackFullName.ifBlank { "Titulaire IDMuslim" }
        val country = profile?.issuingCountry?.takeIf { it.isNotBlank() }
            ?: profile?.residency?.takeIf { it.isNotBlank() }
            ?: fallbackCountry.ifBlank { "France" }
        val expiry = profile?.expiryDate?.takeIf { it.isNotBlank() } ?: fallbackExpiry

        val passportNum = profile?.passportNumber?.takeIf { it.isNotBlank() } ?: fallbackPassport
        if (passportNum.isNotBlank() && passportNum.trim().lowercase() !in existingNumbers) {
            saveDocument(
                WalletDocumentEntity(
                    id = "prof_passport_${UUID.nameUUIDFromBytes(passportNum.toByteArray())}",
                    title = "Passeport Principal",
                    category = WalletDocCategory.PASSPORT.code,
                    documentNumber = passportNum.trim(),
                    holderFullName = holder,
                    issuingCountry = country,
                    issuingAuthority = "Autorité Consulaire / État",
                    issueDate = "",
                    expiryDate = expiry,
                    notes = "Importé depuis votre profil vérifié IDMuslim",
                    isVerified = profile?.isVerified ?: true,
                    isFavorite = true
                )
            )
            importedCount++
        }

        val licenseNum = profile?.licenseNumber?.takeIf { it.isNotBlank() } ?: fallbackLicense
        if (licenseNum.isNotBlank() && licenseNum.trim().lowercase() !in existingNumbers) {
            saveDocument(
                WalletDocumentEntity(
                    id = "prof_license_${UUID.nameUUIDFromBytes(licenseNum.toByteArray())}",
                    title = "Permis de Conduire",
                    category = WalletDocCategory.DRIVER_LICENSE.code,
                    documentNumber = licenseNum.trim(),
                    holderFullName = holder,
                    issuingCountry = country,
                    issuingAuthority = "Préfecture / ANTS",
                    issueDate = "",
                    expiryDate = expiry,
                    notes = "Importé depuis votre profil IDMuslim",
                    isVerified = profile?.isVerified ?: false
                )
            )
            importedCount++
        }

        val primaryDocNum = profile?.docNumber?.takeIf { it.isNotBlank() } ?: fallbackDocNumber
        if (primaryDocNum.isNotBlank() &&
            primaryDocNum != passportNum &&
            primaryDocNum != licenseNum &&
            primaryDocNum.trim().lowercase() !in existingNumbers
        ) {
            val rawType = profile?.docType?.takeIf { it.isNotBlank() } ?: fallbackDocType
            val mappedCategory = when {
                rawType.contains("pass", ignoreCase = true) -> WalletDocCategory.PASSPORT
                rawType.contains("séjour", ignoreCase = true) || rawType.contains("residence", ignoreCase = true) -> WalletDocCategory.RESIDENCE_PERMIT
                rawType.contains("permis", ignoreCase = true) || rawType.contains("license", ignoreCase = true) -> WalletDocCategory.DRIVER_LICENSE
                else -> WalletDocCategory.NATIONAL_ID
            }
            saveDocument(
                WalletDocumentEntity(
                    id = "prof_doc_${UUID.nameUUIDFromBytes(primaryDocNum.toByteArray())}",
                    title = rawType.ifBlank { mappedCategory.labelFr },
                    category = mappedCategory.code,
                    documentNumber = primaryDocNum.trim(),
                    holderFullName = holder,
                    issuingCountry = country,
                    issuingAuthority = "Administration Nationale",
                    issueDate = "",
                    expiryDate = expiry,
                    notes = "Synchronisé depuis votre identité privée",
                    isVerified = profile?.isVerified ?: false
                )
            )
            importedCount++
        }

        scannedDocs.forEach { doc ->
            val syntheticRef = "SCAN-${doc.id.take(8).uppercase()}"
            if (syntheticRef.lowercase() !in existingNumbers) {
                saveDocument(
                    WalletDocumentEntity(
                        id = "scan_${doc.id}",
                        title = doc.name.ifBlank { "Pièce Numérisée OCR" },
                        category = WalletDocCategory.NATIONAL_ID.code,
                        documentNumber = syntheticRef,
                        holderFullName = holder,
                        issuingCountry = country,
                        issuingAuthority = "Numérisation IDMuslim Shield",
                        issueDate = SimpleDateFormatHelper.formatTimestamp(doc.uploadedAt),
                        expiryDate = expiry,
                        notes = "Statut de vérification : ${doc.status}",
                        isVerified = doc.status.equals("VERIFIED", ignoreCase = true)
                    )
                )
                importedCount++
            }
        }

        importedCount
    }
}

private object SimpleDateFormatHelper {
    fun formatTimestamp(ts: Long): String {
        if (ts <= 0L) return ""
        return try {
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(ts))
        } catch (_: Exception) {
            ""
        }
    }
}
