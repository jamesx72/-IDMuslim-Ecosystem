package com.example.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DocumentEntity
import com.example.data.UserProfileEntity
import com.example.data.wallet.EncryptedVaultFileEntity
import com.example.data.wallet.SecureWalletDatabase
import com.example.data.wallet.SensitiveInfoEntity
import com.example.data.wallet.SqlCipherVaultTelemetry
import com.example.data.wallet.WalletDocumentEntity
import com.example.data.wallet.WalletRepository
import com.example.security.KeystoreManager
import com.example.security.WalletVaultKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

sealed class VaultFilePreviewState {
    object Hidden : VaultFilePreviewState()
    data class Loading(val file: EncryptedVaultFileEntity) : VaultFilePreviewState()
    data class ImageReady(val file: EncryptedVaultFileEntity, val bitmap: Bitmap) : VaultFilePreviewState()
    data class PdfReady(val file: EncryptedVaultFileEntity, val pages: List<Bitmap>) : VaultFilePreviewState()
    data class Error(val file: EncryptedVaultFileEntity, val message: String) : VaultFilePreviewState()
}

data class DigitalWalletUiState(
    val isVaultLocked: Boolean = false,
    val isPrivacyMaskEnabled: Boolean = true,
    val isScreenCaptureBlocked: Boolean = true,
    val selectedTabIndex: Int = 0,
    val searchQuery: String = "",
    val selectedDocCategory: String? = null,
    val selectedInfoCategory: String? = null,
    val revealedItemIds: Set<String> = emptySet(),
    val telemetry: SqlCipherVaultTelemetry? = null,
    val isProcessingKeyRotation: Boolean = false,
    val isUploadingEncryptedDoc: Boolean = false,
    val hasCustomPin: Boolean = false,
    val eccFingerprint: String = "",
    val statusMessage: String? = null
)

class DigitalWalletViewModel(
    private val appContext: Context,
    private val repository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DigitalWalletUiState(
            hasCustomPin = WalletVaultKeyManager.hasCustomVaultPin(appContext),
            eccFingerprint = KeystoreManager.getVaultEciesFingerprintSha256(appContext)
        )
    )
    val uiState: StateFlow<DigitalWalletUiState> = _uiState.asStateFlow()

    val documents: StateFlow<List<WalletDocumentEntity>> = repository.allDocuments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sensitiveItems: StateFlow<List<SensitiveInfoEntity>> = repository.allSensitiveInfo.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val encryptedFiles: StateFlow<List<EncryptedVaultFileEntity>> = repository.allEncryptedFiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _previewState = MutableStateFlow<VaultFilePreviewState>(VaultFilePreviewState.Hidden)
    val previewState: StateFlow<VaultFilePreviewState> = _previewState.asStateFlow()

    private val autoHideJobs = mutableMapOf<String, Job>()

    init {
        refreshTelemetry()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
        if (index == 2) {
            refreshTelemetry()
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectDocCategoryFilter(categoryCode: String?) {
        _uiState.update { current ->
            val next = if (current.selectedDocCategory == categoryCode) null else categoryCode
            current.copy(selectedDocCategory = next)
        }
    }

    fun selectInfoCategoryFilter(categoryCode: String?) {
        _uiState.update { current ->
            val next = if (current.selectedInfoCategory == categoryCode) null else categoryCode
            current.copy(selectedInfoCategory = next)
        }
    }

    fun lockVault() {
        autoHideJobs.values.forEach { it.cancel() }
        autoHideJobs.clear()
        _uiState.update {
            it.copy(
                isVaultLocked = true,
                isPrivacyMaskEnabled = true,
                revealedItemIds = emptySet()
            )
        }
    }

    fun unlockVault() {
        _uiState.update { it.copy(isVaultLocked = false) }
        refreshTelemetry()
    }

    fun unlockWithPin(pin: String): Boolean {
        val verified = WalletVaultKeyManager.verifyCustomVaultPin(appContext, pin)
        if (verified) {
            unlockVault()
        }
        return verified
    }

    fun saveCustomPin(pin: String) {
        WalletVaultKeyManager.setCustomVaultPin(appContext, pin)
        _uiState.update {
            it.copy(
                hasCustomPin = true,
                statusMessage = "Code PIN du coffre enregistré et haché (SHA-256 salé)."
            )
        }
    }

    fun toggleGlobalPrivacyMask() {
        _uiState.update { current ->
            val nextMask = !current.isPrivacyMaskEnabled
            current.copy(
                isPrivacyMaskEnabled = nextMask,
                revealedItemIds = if (nextMask) emptySet() else current.revealedItemIds
            )
        }
    }

    fun toggleScreenCaptureProtection(enabled: Boolean) {
        _uiState.update { it.copy(isScreenCaptureBlocked = enabled) }
    }

    /**
     * Reveals a single item's sensitive secret for 15 seconds, then automatically re-masks it.
     */
    fun toggleItemReveal(id: String) {
        val currentlyRevealed = id in _uiState.value.revealedItemIds
        if (currentlyRevealed) {
            autoHideJobs.remove(id)?.cancel()
            _uiState.update { it.copy(revealedItemIds = it.revealedItemIds - id) }
        } else {
            _uiState.update { it.copy(revealedItemIds = it.revealedItemIds + id) }
            autoHideJobs[id]?.cancel()
            autoHideJobs[id] = viewModelScope.launch {
                delay(15_000L)
                _uiState.update { state -> state.copy(revealedItemIds = state.revealedItemIds - id) }
                autoHideJobs.remove(id)
            }
        }
    }

    fun saveIdentityDocument(
        existingId: String?,
        title: String,
        category: String,
        documentNumber: String,
        holderFullName: String,
        issuingCountry: String,
        issuingAuthority: String,
        issueDate: String,
        expiryDate: String,
        mrzCode: String,
        attachmentBase64: String?,
        notes: String,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = WalletDocumentEntity(
                id = existingId ?: UUID.randomUUID().toString(),
                title = title.trim(),
                category = category,
                documentNumber = documentNumber.trim(),
                holderFullName = holderFullName.trim(),
                issuingCountry = issuingCountry.trim(),
                issuingAuthority = issuingAuthority.trim(),
                issueDate = issueDate.trim(),
                expiryDate = expiryDate.trim(),
                mrzCode = mrzCode.trim(),
                attachmentBase64 = attachmentBase64,
                notes = notes.trim(),
                isVerified = true,
                isFavorite = isFavorite,
                createdAt = now,
                updatedAt = now
            )
            repository.saveDocument(entity)
            refreshTelemetry()
            _uiState.update {
                it.copy(statusMessage = "Pièce d'identité chiffrée dans SQLCipher AES-256.")
            }
        }
    }

    fun toggleDocumentFavorite(doc: WalletDocumentEntity) {
        viewModelScope.launch {
            repository.toggleDocumentFavorite(doc.id, !doc.isFavorite)
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
            refreshTelemetry()
            _uiState.update { it.copy(statusMessage = "Pièce supprimée du coffre chiffré.") }
        }
    }

    fun saveSensitiveInfo(
        existingId: String?,
        title: String,
        category: String,
        primaryLabel: String,
        secretValue: String,
        secondaryLabel: String,
        secondaryValue: String,
        confidentialNotes: String,
        isPinned: Boolean,
        requiresBiometricReveal: Boolean
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val item = SensitiveInfoEntity(
                id = existingId ?: UUID.randomUUID().toString(),
                title = title.trim(),
                category = category,
                primaryLabel = primaryLabel.trim(),
                secretValue = secretValue.trim(),
                secondaryLabel = secondaryLabel.trim(),
                secondaryValue = secondaryValue.trim(),
                confidentialNotes = confidentialNotes.trim(),
                isPinned = isPinned,
                requiresBiometricReveal = requiresBiometricReveal,
                createdAt = now,
                updatedAt = now
            )
            repository.saveSensitiveInfo(item)
            refreshTelemetry()
            _uiState.update {
                it.copy(statusMessage = "Information sensible enregistrée et chiffrée (SQLCipher).")
            }
        }
    }

    fun toggleSensitivePinned(item: SensitiveInfoEntity) {
        viewModelScope.launch {
            repository.toggleSensitivePinned(item.id, !item.isPinned)
        }
    }

    fun deleteSensitiveInfo(id: String) {
        viewModelScope.launch {
            repository.deleteSensitiveInfo(id)
            refreshTelemetry()
            _uiState.update { it.copy(statusMessage = "Secret effacé du stockage SQLCipher.") }
        }
    }

    fun importCredentialsFromProfile(
        profile: UserProfileEntity?,
        fallbackFullName: String,
        fallbackPassport: String,
        fallbackLicense: String,
        fallbackDocType: String,
        fallbackDocNumber: String,
        fallbackCountry: String,
        fallbackExpiry: String,
        scannedDocs: List<DocumentEntity>
    ) {
        viewModelScope.launch {
            val count = repository.importFromProfileAndScannedDocs(
                profile = profile,
                fallbackFullName = fallbackFullName,
                fallbackPassport = fallbackPassport,
                fallbackLicense = fallbackLicense,
                fallbackDocType = fallbackDocType,
                fallbackDocNumber = fallbackDocNumber,
                fallbackCountry = fallbackCountry,
                fallbackExpiry = fallbackExpiry,
                scannedDocs = scannedDocs
            )
            refreshTelemetry()
            val message = if (count > 0) {
                "$count pièce(s) synchronisée(s) vers le coffre SQLCipher."
            } else {
                "Aucune nouvelle pièce à importer (renseignez votre profil ou ajoutez une pièce)."
            }
            _uiState.update { it.copy(statusMessage = message) }
        }
    }

    fun rotateSqlCipherKey() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingKeyRotation = true) }
            val success = repository.rotateMasterKey(appContext)
            val telemetry = repository.loadTelemetry(appContext)
            _uiState.update {
                it.copy(
                    isProcessingKeyRotation = false,
                    telemetry = telemetry,
                    statusMessage = if (success) {
                        "Clé SQLCipher 256-bit régénérée avec succès (PRAGMA rekey)."
                    } else {
                        "Échec lors de la rotation de clé SQLCipher."
                    }
                )
            }
        }
    }

    fun uploadEncryptedDocument(
        uri: Uri,
        title: String,
        docCategory: String,
        uploadToCloud: Boolean = true,
        onComplete: (EncryptedVaultFileEntity?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingEncryptedDoc = true) }
            val result = repository.uploadEncryptedUri(
                context = appContext,
                uri = uri,
                title = title,
                docCategory = docCategory,
                uploadToCloud = uploadToCloud
            )
            refreshTelemetry()
            result.fold(
                onSuccess = { entity ->
                    val statusMsg = if (entity.syncStatus == "SYNCED_CLOUD") {
                        "Document chiffré (ECC P-256) stocké dans SQLCipher & Firebase Storage."
                    } else {
                        "Document chiffré (ECC P-256) enregistré localement dans SQLCipher."
                    }
                    _uiState.update {
                        it.copy(
                            isUploadingEncryptedDoc = false,
                            statusMessage = statusMsg
                        )
                    }
                    onComplete(entity)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isUploadingEncryptedDoc = false,
                            statusMessage = err.localizedMessage ?: "Échec du chiffrement du document."
                        )
                    }
                    onComplete(null)
                }
            )
        }
    }

    fun openEncryptedDocumentPreview(file: EncryptedVaultFileEntity) {
        viewModelScope.launch {
            _previewState.value = VaultFilePreviewState.Loading(file)
            val decryptResult = repository.decryptVaultFileBytes(appContext, file)
            decryptResult.fold(
                onSuccess = { plainBytes ->
                    if (file.isPdf) {
                        val pages = renderPdfPagesInMemory(plainBytes)
                        java.util.Arrays.fill(plainBytes, 0.toByte())
                        if (pages.isNotEmpty()) {
                            _previewState.value = VaultFilePreviewState.PdfReady(file, pages)
                        } else {
                            _previewState.value = VaultFilePreviewState.Error(file, "Impossible de décoder les pages du PDF.")
                        }
                    } else {
                        val bitmap = BitmapFactory.decodeByteArray(plainBytes, 0, plainBytes.size)
                        java.util.Arrays.fill(plainBytes, 0.toByte())
                        if (bitmap != null) {
                            _previewState.value = VaultFilePreviewState.ImageReady(file, bitmap)
                        } else {
                            _previewState.value = VaultFilePreviewState.Error(file, "Image déchiffrée illisible.")
                        }
                    }
                },
                onFailure = { err ->
                    _previewState.value = VaultFilePreviewState.Error(
                        file,
                        err.localizedMessage ?: "Erreur lors du déchiffrement ECC."
                    )
                }
            )
        }
    }

    fun dismissDocumentPreview() {
        val current = _previewState.value
        if (current is VaultFilePreviewState.ImageReady) {
            current.bitmap.recycle()
        } else if (current is VaultFilePreviewState.PdfReady) {
            current.pages.forEach { it.recycle() }
        }
        _previewState.value = VaultFilePreviewState.Hidden
    }

    fun retryEncryptedFileCloudSync(file: EncryptedVaultFileEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingEncryptedDoc = true) }
            val result = repository.retryEncryptedFileCloudSync(file)
            _uiState.update {
                it.copy(
                    isUploadingEncryptedDoc = false,
                    statusMessage = result.fold(
                        onSuccess = { updated ->
                            if (updated.syncStatus == "SYNCED_CLOUD") {
                                "Fichier chiffré synchronisé avec Firebase Storage."
                            } else {
                                "Synchronisation Firebase Storage en attente (connectez-vous ou vérifiez le réseau)."
                            }
                        },
                        onFailure = { err -> err.localizedMessage ?: "Synchronisation impossible." }
                    )
                )
            }
        }
    }

    fun deleteEncryptedDocument(file: EncryptedVaultFileEntity) {
        viewModelScope.launch {
            repository.deleteEncryptedFile(file)
            refreshTelemetry()
            _uiState.update {
                it.copy(statusMessage = "Document chiffré supprimé localement et sur Firebase Storage.")
            }
        }
    }

    private suspend fun renderPdfPagesInMemory(pdfBytes: ByteArray): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        var tempPdf: File? = null
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            tempPdf = File.createTempFile("idm_dec_", ".pdf", appContext.cacheDir)
            tempPdf.writeBytes(pdfBytes)
            pfd = ParcelFileDescriptor.open(tempPdf, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount.coerceAtMost(12)
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                val targetWidth = 1080
                val ratio = page.height.toFloat() / page.width.coerceAtLeast(1).toFloat()
                val targetHeight = (targetWidth * ratio).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                bitmaps.add(bitmap)
            }
        } catch (_: Throwable) {
            // Return whatever pages rendered cleanly
        } finally {
            try { renderer?.close() } catch (_: Throwable) {}
            try { pfd?.close() } catch (_: Throwable) {}
            try {
                if (tempPdf != null && tempPdf.exists()) {
                    tempPdf.writeBytes(ByteArray(0))
                    tempPdf.delete()
                }
            } catch (_: Throwable) {}
        }
        bitmaps
    }

    fun purgeEntireVault() {
        viewModelScope.launch {
            repository.purgeEntireVault()
            refreshTelemetry()
            _uiState.update {
                it.copy(
                    revealedItemIds = emptySet(),
                    statusMessage = "Purge cryptographique effectuée : toutes les entrées ont été effacées."
                )
            }
        }
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            val latest = repository.loadTelemetry(appContext)
            _uiState.update { it.copy(telemetry = latest) }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appCtx = context.applicationContext
            val db = SecureWalletDatabase.getInstance(appCtx)
            val repo = WalletRepository(db.walletDao())
            return DigitalWalletViewModel(appCtx, repo) as T
        }
    }
}
