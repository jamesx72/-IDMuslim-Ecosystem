package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DocumentEntity
import com.example.data.UserProfileEntity
import com.example.data.wallet.SecureWalletDatabase
import com.example.data.wallet.SensitiveInfoEntity
import com.example.data.wallet.SqlCipherVaultTelemetry
import com.example.data.wallet.WalletDocumentEntity
import com.example.data.wallet.WalletRepository
import com.example.security.WalletVaultKeyManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

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
    val hasCustomPin: Boolean = false,
    val statusMessage: String? = null
)

class DigitalWalletViewModel(
    private val appContext: Context,
    private val repository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DigitalWalletUiState(
            hasCustomPin = WalletVaultKeyManager.hasCustomVaultPin(appContext)
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
