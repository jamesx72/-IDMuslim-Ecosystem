package com.example.security

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.wallet.EncryptedVaultFileEntity
import com.example.data.wallet.WalletDao
import com.example.data.wallet.encryptedFields
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Arrays
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Manages end-to-end client-side ECC ECIES encryption (P-256 ECDH + HKDF-SHA256 + AES-256-GCM)
 * for PDF and image identification documents before uploading ciphertext to Firebase Storage and
 * persisting encrypted metadata + integrity proofs inside SQLCipher.
 */
object EncryptedDocumentVaultManager {

    private const val TAG = "EncryptedDocVaultMgr"
    private const val VAULT_SUBDIR = "encrypted_vault_docs"
    private const val MAX_FILE_SIZE_BYTES = 15L * 1024L * 1024L // 15 MB
    private const val CLOUD_TIMEOUT_MS = 20_000L

    suspend fun encryptAndStoreUri(
        context: Context,
        uri: Uri,
        customTitle: String,
        docCategory: String,
        walletDao: WalletDao,
        cryptoManager: CryptoManager = CryptoManager.getInstance(),
        uploadToCloud: Boolean = true
    ): Result<EncryptedVaultFileEntity> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val fileName = resolveFileName(context, uri)
        val mimeType = resolver.getType(uri)
            ?: inferMimeType(fileName)

        val plainBytes = readUriBytesLimited(context, uri)
            ?: return@withContext Result.failure(IllegalArgumentException("Impossible de lire le fichier sélectionné ou taille > 15 Mo."))

        encryptAndStoreBytes(
            context = context,
            plainBytes = plainBytes,
            fileName = fileName,
            mimeType = mimeType,
            customTitle = customTitle,
            docCategory = docCategory,
            walletDao = walletDao,
            cryptoManager = cryptoManager,
            uploadToCloud = uploadToCloud
        )
    }

    suspend fun encryptAndStoreBytes(
        context: Context,
        plainBytes: ByteArray,
        fileName: String,
        mimeType: String,
        customTitle: String,
        docCategory: String,
        walletDao: WalletDao,
        cryptoManager: CryptoManager = CryptoManager.getInstance(),
        uploadToCloud: Boolean = true
    ): Result<EncryptedVaultFileEntity> = withContext(Dispatchers.IO) {
        if (plainBytes.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Le fichier sélectionné est vide."))
        }
        if (plainBytes.size > MAX_FILE_SIZE_BYTES) {
            return@withContext Result.failure(IllegalArgumentException("Le fichier dépasse la limite de 15 Mo."))
        }

        val resolvedMime = normalizeMimeType(mimeType, fileName, plainBytes)
        if (!resolvedMime.startsWith("application/pdf") && !resolvedMime.startsWith("image/")) {
            return@withContext Result.failure(IllegalArgumentException("Format non supporté. Sélectionnez un PDF ou une image."))
        }

        val workingPlain = plainBytes.copyOf()
        val now = System.currentTimeMillis()
        val docId = UUID.randomUUID().toString()
        val uid = safeCurrentUid() ?: "local_vault"

        try {
            val sha256PlainHex = EccCryptoEngine.sha256Hex(workingPlain)
            val aad = buildAad(uid, docId, resolvedMime)

            val cipherBytes = KeystoreManager.encryptBytesForVault(context, workingPlain, aad)
                .getOrElse { err -> return@withContext Result.failure(err) }

            val sha256CipherHex = EccCryptoEngine.sha256Hex(cipherBytes)
            val fingerprint = KeystoreManager.getVaultEciesFingerprintSha256(context)
            val manifest = "$docId|$sha256PlainHex|$uid|$now"
            val signatureBase64 = KeystoreManager.signData(manifest) ?: ""

            val dir = File(context.filesDir, VAULT_SUBDIR).apply { if (!exists()) mkdirs() }
            val encryptedFile = File(dir, "$docId.enc")
            encryptedFile.writeBytes(cipherBytes)

            val storagePath = "encrypted_documents/$uid/$docId.enc"
            val title = customTitle.trim().ifBlank { fileName.substringBeforeLast('.') }.ifBlank { "Document Chiffré" }

            val initialEntity = EncryptedVaultFileEntity(
                id = docId,
                userId = uid,
                title = title,
                docCategory = docCategory,
                fileName = fileName,
                mimeType = resolvedMime,
                originalSizeBytes = workingPlain.size.toLong(),
                encryptedSizeBytes = cipherBytes.size.toLong(),
                sha256PlaintextHex = sha256PlainHex,
                sha256CiphertextHex = sha256CipherHex,
                eccAlgorithm = "ECDH-P256+HKDF-SHA256+AES-256-GCM",
                eccPublicKeyFingerprint = fingerprint,
                ecdsaSignatureBase64 = signatureBase64,
                localEncryptedPath = encryptedFile.absolutePath,
                firebaseStoragePath = storagePath,
                firebaseDownloadUrl = "",
                syncStatus = "ENCRYPTED_LOCAL",
                createdAt = now,
                updatedAt = now
            )

            walletDao.upsertEncryptedFile(initialEntity.encryptedFields(cryptoManager))

            val finalEntity = if (uploadToCloud && safeCurrentUid() != null) {
                uploadEncryptedBlobToFirebase(
                    entity = initialEntity,
                    cipherBytes = cipherBytes,
                    walletDao = walletDao,
                    cryptoManager = cryptoManager
                )
            } else {
                initialEntity
            }

            Result.success(finalEntity)
        } catch (e: Exception) {
            Log.e(TAG, "Encrypted document storage failed: ${e.javaClass.simpleName}", e)
            Result.failure(e)
        } finally {
            Arrays.fill(workingPlain, 0.toByte())
        }
    }

    suspend fun syncLocalEncryptedFileToCloud(
        entity: EncryptedVaultFileEntity,
        walletDao: WalletDao,
        cryptoManager: CryptoManager = CryptoManager.getInstance()
    ): Result<EncryptedVaultFileEntity> = withContext(Dispatchers.IO) {
        if (safeCurrentUid() == null) {
            return@withContext Result.failure(IllegalStateException("Connectez-vous à votre compte pour synchroniser avec Firebase Storage."))
        }
        val localFile = File(entity.localEncryptedPath)
        if (!localFile.exists()) {
            return@withContext Result.failure(IllegalStateException("Fichier chiffré local introuvable."))
        }
        val cipherBytes = localFile.readBytes()
        val updated = uploadEncryptedBlobToFirebase(entity, cipherBytes, walletDao, cryptoManager)
        Result.success(updated)
    }

    suspend fun decryptDocumentBytes(
        context: Context,
        entity: EncryptedVaultFileEntity
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            var cipherBytes: ByteArray? = null
            val localFile = File(entity.localEncryptedPath)
            if (localFile.exists() && localFile.length() > 0L) {
                cipherBytes = localFile.readBytes()
            } else if (entity.firebaseStoragePath.isNotBlank() && safeCurrentUid() != null) {
                cipherBytes = withTimeoutOrNull(CLOUD_TIMEOUT_MS) {
                    FirebaseStorage.getInstance().reference
                        .child(entity.firebaseStoragePath)
                        .getBytes(MAX_FILE_SIZE_BYTES + 4096L)
                        .awaitTask()
                }
                if (cipherBytes != null) {
                    localFile.parentFile?.mkdirs()
                    localFile.writeBytes(cipherBytes)
                }
            }

            val rawEncrypted = cipherBytes
                ?: return@withContext Result.failure(IllegalStateException("Contenu chiffré indisponible localement et hors-ligne."))

            val actualCipherHash = EccCryptoEngine.sha256Hex(rawEncrypted)
            if (!actualCipherHash.equals(entity.sha256CiphertextHex, ignoreCase = true)) {
                return@withContext Result.failure(SecurityException("Échec du contrôle d'intégrité SHA-256 du fichier chiffré."))
            }

            val aad = buildAad(entity.userId, entity.id, entity.mimeType)
            val plainBytes = KeystoreManager.decryptBytesFromVault(context, rawEncrypted, aad)
                .getOrElse { err -> return@withContext Result.failure(err) }

            val actualPlainHash = EccCryptoEngine.sha256Hex(plainBytes)
            if (!actualPlainHash.equals(entity.sha256PlaintextHex, ignoreCase = true)) {
                Arrays.fill(plainBytes, 0.toByte())
                return@withContext Result.failure(SecurityException("L'empreinte SHA-256 du document déchiffré ne correspond pas."))
            }

            Result.success(plainBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Document decryption failed: ${e.javaClass.simpleName}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteEncryptedDocument(
        entity: EncryptedVaultFileEntity,
        walletDao: WalletDao
    ) = withContext(Dispatchers.IO) {
        try {
            val localFile = File(entity.localEncryptedPath)
            if (localFile.exists()) {
                val len = localFile.length().coerceAtMost(2L * 1024L * 1024L).toInt()
                if (len > 0) {
                    localFile.writeBytes(ByteArray(len))
                }
                localFile.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Local file secure wipe warning: ${e.javaClass.simpleName}")
        }

        walletDao.deleteEncryptedFileById(entity.id)

        val uid = safeCurrentUid()
        if (uid != null) {
            try {
                if (entity.firebaseStoragePath.isNotBlank()) {
                    FirebaseStorage.getInstance().reference.child(entity.firebaseStoragePath).delete()
                }
            } catch (_: Exception) {}
            try {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .collection("documents").document(entity.id).delete()
            } catch (_: Exception) {}
        }
    }

    private suspend fun uploadEncryptedBlobToFirebase(
        entity: EncryptedVaultFileEntity,
        cipherBytes: ByteArray,
        walletDao: WalletDao,
        cryptoManager: CryptoManager
    ): EncryptedVaultFileEntity {
        val uid = safeCurrentUid() ?: return entity
        return try {
            val storageRef = FirebaseStorage.getInstance().reference.child(entity.firebaseStoragePath)
            val metadata = StorageMetadata.Builder()
                .setContentType("application/octet-stream")
                .setCustomMetadata("eccAlgorithm", entity.eccAlgorithm)
                .setCustomMetadata("sha256Ciphertext", entity.sha256CiphertextHex)
                .setCustomMetadata("eccFingerprint", entity.eccPublicKeyFingerprint)
                .build()

            val downloadUrl = withTimeoutOrNull(CLOUD_TIMEOUT_MS) {
                storageRef.putBytes(cipherBytes, metadata).awaitTask()
                storageRef.downloadUrl.awaitTask().toString()
            } ?: return entity

            val now = System.currentTimeMillis()
            val encryptedUrl = cryptoManager.encrypt(downloadUrl) ?: downloadUrl
            walletDao.updateEncryptedFileSyncStatus(
                id = entity.id,
                status = "SYNCED_CLOUD",
                downloadUrl = encryptedUrl,
                updatedAt = now
            )

            val docPayload = hashMapOf<String, Any>(
                "id" to entity.id,
                "name" to (cryptoManager.encrypt(entity.title) ?: entity.title),
                "url" to encryptedUrl,
                "storagePath" to entity.firebaseStoragePath,
                "docType" to entity.docCategory,
                "mimeType" to entity.mimeType,
                "eccAlgorithm" to entity.eccAlgorithm,
                "eccFingerprint" to entity.eccPublicKeyFingerprint,
                "sha256PlaintextHex" to entity.sha256PlaintextHex,
                "sha256CiphertextHex" to entity.sha256CiphertextHex,
                "ecdsaSignatureBase64" to entity.ecdsaSignatureBase64,
                "encryptedSizeBytes" to entity.encryptedSizeBytes,
                "status" to "SYNCED_CLOUD",
                "uploadedAt" to now
            )
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("documents").document(entity.id)
                .set(docPayload, SetOptions.merge())

            entity.copy(
                firebaseDownloadUrl = downloadUrl,
                syncStatus = "SYNCED_CLOUD",
                updatedAt = now
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Storage upload skipped/deferred (${e.javaClass.simpleName}); document remains safely encrypted in SQLCipher.")
            entity
        }
    }

    private fun buildAad(uid: String, docId: String, mimeType: String): ByteArray {
        return "$uid|$docId|$mimeType".toByteArray(Charsets.UTF_8)
    }

    private fun safeCurrentUid(): String? = try {
        FirebaseAuth.getInstance().currentUser?.uid
    } catch (_: Throwable) {
        null
    }

    private fun resolveFileName(context: Context, uri: Uri): String {
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        val name = cursor.getString(idx)
                        if (!name.isNullOrBlank()) return name
                    }
                }
            }
        } catch (_: Exception) {}
        return uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            ?: "document_${System.currentTimeMillis()}"
    }

    private fun inferMimeType(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".pdf") -> "application/pdf"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".webp") -> "image/webp"
            else -> "image/jpeg"
        }
    }

    private fun normalizeMimeType(rawMime: String, fileName: String, bytes: ByteArray): String {
        if (bytes.size >= 4 &&
            bytes[0] == '%'.code.toByte() &&
            bytes[1] == 'P'.code.toByte() &&
            bytes[2] == 'D'.code.toByte() &&
            bytes[3] == 'F'.code.toByte()
        ) {
            return "application/pdf"
        }
        return if (rawMime.isNotBlank() && rawMime != "application/octet-stream") {
            rawMime
        } else {
            inferMimeType(fileName)
        }
    }

    private fun readUriBytesLimited(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val out = ByteArrayOutputStream()
                val buf = ByteArray(16 * 1024)
                var total = 0L
                while (true) {
                    val read = input.read(buf)
                    if (read <= 0) break
                    total += read
                    if (total > MAX_FILE_SIZE_BYTES) {
                        return null
                    }
                    out.write(buf, 0, read)
                }
                out.toByteArray()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading content URI: ${e.javaClass.simpleName}")
            null
        }
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exc ->
            if (cont.isActive) cont.resumeWithException(exc)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
}
