package com.example.data.wallet

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.security.CryptoManager
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class WalletDocCategory(
    val code: String,
    val labelFr: String,
    val labelEn: String,
    val cardThemeIndex: Int
) {
    PASSPORT("PASSPORT", "Passeport Biométrique", "Biometric Passport", 0),
    NATIONAL_ID("NATIONAL_ID", "Carte Nationale d'Identité", "National ID Card", 1),
    RESIDENCE_PERMIT("RESIDENCE_PERMIT", "Titre de Séjour", "Residence Permit", 2),
    DRIVER_LICENSE("DRIVER_LICENSE", "Permis de Conduire", "Driver's License", 5),
    HAJJ_UMRAH("HAJJ_UMRAH", "Visa Hajj / Omra & Nusuk", "Hajj / Umrah Visa", 3),
    HEALTH_CARD("HEALTH_CARD", "Carte Vitale / Santé", "Health Insurance Card", 4),
    CIVIL_FAMILY("CIVIL_FAMILY", "Livret de Famille / Acte", "Family / Civil Record", 1);

    fun localizedLabel(language: String): String =
        if (language.startsWith("en", ignoreCase = true)) labelEn else labelFr

    companion object {
        fun fromCode(code: String): WalletDocCategory =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: NATIONAL_ID
    }
}

enum class SensitiveInfoCategory(
    val code: String,
    val labelFr: String,
    val labelEn: String,
    val defaultPrimaryHintFr: String,
    val defaultSecondaryHintFr: String
) {
    MEDICAL_EMERGENCY(
        "MEDICAL_EMERGENCY",
        "Urgence Médicale & Santé",
        "Medical & Emergency",
        "Groupe sanguin / Allergies / Traitement",
        "Contact d'urgence (ICE) & Téléphone"
    ),
    BANKING_FINANCE(
        "BANKING_FINANCE",
        "RIB / IBAN & Finance",
        "Banking & IBAN",
        "Numéro IBAN / Compte",
        "Code BIC / SWIFT & Banque"
    ),
    GOVERNMENT_ADMIN(
        "GOVERNMENT_ADMIN",
        "Numéros Administratifs",
        "Government & Tax IDs",
        "N° Sécurité Sociale / Identifiant Fiscal",
        "Organisme de rattachement / Centre"
    ),
    SECURITY_PIN_2FA(
        "SECURITY_PIN_2FA",
        "Codes PIN & Clés 2FA",
        "PINs & Recovery Keys",
        "Code PIN / Clé de récupération",
        "Indice ou identifiant associé"
    ),
    CONFIDENTIAL_NOTE(
        "CONFIDENTIAL_NOTE",
        "Note Confidentielle",
        "Confidential Note",
        "Contenu confidentiel chiffré",
        "Référence complémentaire"
    );

    fun localizedLabel(language: String): String =
        if (language.startsWith("en", ignoreCase = true)) labelEn else labelFr

    companion object {
        fun fromCode(code: String): SensitiveInfoCategory =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: CONFIDENTIAL_NOTE
    }
}

@Entity(tableName = "wallet_identity_documents")
data class WalletDocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val documentNumber: String,
    val holderFullName: String,
    val issuingCountry: String,
    val issuingAuthority: String,
    val issueDate: String,
    val expiryDate: String,
    val mrzCode: String = "",
    val attachmentBase64: String? = null,
    val notes: String = "",
    val isVerified: Boolean = true,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun maskedDocumentNumber(): String = maskSensitiveString(documentNumber)

    fun integrityHashPreview(): String {
        val raw = "$id|$category|$documentNumber|$holderFullName|$expiryDate"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.take(8).joinToString("") { "%02x".format(it) }.uppercase(Locale.ROOT)
    }
}

@Entity(tableName = "wallet_sensitive_info")
data class SensitiveInfoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val primaryLabel: String,
    val secretValue: String,
    val secondaryLabel: String = "",
    val secondaryValue: String = "",
    val confidentialNotes: String = "",
    val isPinned: Boolean = false,
    val requiresBiometricReveal: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun maskedSecretValue(): String = maskSensitiveString(secretValue)
}

data class SqlCipherVaultTelemetry(
    val isSqlCipherActive: Boolean,
    val cipherVersion: String,
    val cipherAlgorithm: String,
    val kdfIterations: Int,
    val pageSize: Int,
    val databaseFileName: String,
    val databaseSizeBytes: Long,
    val isDiskHeaderEncrypted: Boolean,
    val encryptedHeaderHexPreview: String,
    val keyFingerprintSha256: String,
    val keyLastRotatedAt: Long
)

enum class ExpiryHealthStatus {
    VALID,
    EXPIRING_SOON,
    EXPIRED,
    NO_EXPIRY
}

data class ExpiryAssessment(
    val status: ExpiryHealthStatus,
    val labelFr: String,
    val daysRemaining: Long? = null
)

fun evaluateExpiryDate(expiryDate: String): ExpiryAssessment {
    val trimmed = expiryDate.trim()
    if (trimmed.isBlank()) {
        return ExpiryAssessment(ExpiryHealthStatus.NO_EXPIRY, "Permanent")
    }
    val formats = listOf("dd/MM/yyyy", "yyyy-MM-dd", "MM/yyyy")
    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
            val parsed = sdf.parse(trimmed) ?: continue
            val diffMillis = parsed.time - System.currentTimeMillis()
            val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
            return when {
                days < 0 -> ExpiryAssessment(ExpiryHealthStatus.EXPIRED, "Expiré", days)
                days <= 90 -> ExpiryAssessment(ExpiryHealthStatus.EXPIRING_SOON, "Expire dans ${days}j", days)
                else -> ExpiryAssessment(ExpiryHealthStatus.VALID, "Valide • ${days}j", days)
            }
        } catch (_: Exception) {
            // Try next format
        }
    }
    return ExpiryAssessment(ExpiryHealthStatus.VALID, "Valide ($trimmed)")
}

fun maskSensitiveString(raw: String): String {
    val clean = raw.trim()
    if (clean.isEmpty()) return "••••••••"
    if (clean.length <= 4) return "••••"
    val prefix = clean.take(2)
    val suffix = clean.takeLast(2)
    return "$prefix •••• •••• $suffix"
}

/**
 * Defense-in-depth field-level AES-256-GCM encryption on top of SQLCipher full-database encryption.
 */
fun WalletDocumentEntity.encryptedFields(crypto: CryptoManager = CryptoManager.getInstance()): WalletDocumentEntity {
    return copy(
        documentNumber = crypto.encrypt(documentNumber) ?: documentNumber,
        holderFullName = crypto.encrypt(holderFullName) ?: holderFullName,
        mrzCode = crypto.encrypt(mrzCode) ?: mrzCode,
        notes = crypto.encrypt(notes) ?: notes
    )
}

fun WalletDocumentEntity.decryptedFields(crypto: CryptoManager = CryptoManager.getInstance()): WalletDocumentEntity {
    return copy(
        documentNumber = crypto.decrypt(documentNumber) ?: documentNumber,
        holderFullName = crypto.decrypt(holderFullName) ?: holderFullName,
        mrzCode = crypto.decrypt(mrzCode) ?: mrzCode,
        notes = crypto.decrypt(notes) ?: notes
    )
}

fun SensitiveInfoEntity.encryptedFields(crypto: CryptoManager = CryptoManager.getInstance()): SensitiveInfoEntity {
    return copy(
        secretValue = crypto.encrypt(secretValue) ?: secretValue,
        secondaryValue = crypto.encrypt(secondaryValue) ?: secondaryValue,
        confidentialNotes = crypto.encrypt(confidentialNotes) ?: confidentialNotes
    )
}

fun SensitiveInfoEntity.decryptedFields(crypto: CryptoManager = CryptoManager.getInstance()): SensitiveInfoEntity {
    return copy(
        secretValue = crypto.decrypt(secretValue) ?: secretValue,
        secondaryValue = crypto.decrypt(secondaryValue) ?: secondaryValue,
        confidentialNotes = crypto.decrypt(confidentialNotes) ?: confidentialNotes
    )
}
