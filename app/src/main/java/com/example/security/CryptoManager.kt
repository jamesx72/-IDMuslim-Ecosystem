package com.example.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.data.DocumentEntity
import com.example.data.UserProfileEntity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "IDMuslimMasterRoomKey"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val ENCRYPTED_PREFIX = "ENC::"

        @Volatile
        private var INSTANCE: CryptoManager? = null

        fun getInstance(): CryptoManager {
            return INSTANCE ?: synchronized(this) {
                val instance = CryptoManager()
                INSTANCE = instance
                instance
            }
        }
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    private fun getSecretKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    fun encrypt(plainText: String?): String? {
        if (plainText.isNullOrEmpty()) return plainText
        if (plainText.startsWith(ENCRYPTED_PREFIX)) return plainText

        return try {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

            val base64 = Base64.encodeToString(combined, Base64.NO_WRAP)
            "$ENCRYPTED_PREFIX$base64"
        } catch (e: Exception) {
            e.printStackTrace()
            plainText
        }
    }

    fun decrypt(cipherText: String?): String? {
        if (cipherText.isNullOrEmpty()) return cipherText
        if (!cipherText.startsWith(ENCRYPTED_PREFIX)) return cipherText

        return try {
            val rawBase64 = cipherText.removePrefix(ENCRYPTED_PREFIX)
            val combined = Base64.decode(rawBase64, Base64.NO_WRAP)

            if (combined.size <= GCM_IV_LENGTH) return cipherText

            val iv = ByteArray(GCM_IV_LENGTH)
            val cipherBytes = ByteArray(combined.size - GCM_IV_LENGTH)

            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.size)

            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            cipherText.removePrefix(ENCRYPTED_PREFIX)
        }
    }
}

fun UserProfileEntity.encrypted(cryptoManager: CryptoManager = CryptoManager.getInstance()): UserProfileEntity {
    return this.copy(
        fullName = cryptoManager.encrypt(fullName) ?: fullName,
        dob = cryptoManager.encrypt(dob) ?: dob,
        residency = cryptoManager.encrypt(residency) ?: residency,
        passportNumber = cryptoManager.encrypt(passportNumber) ?: passportNumber,
        licenseNumber = cryptoManager.encrypt(licenseNumber) ?: licenseNumber,
        docType = cryptoManager.encrypt(docType) ?: docType,
        docNumber = cryptoManager.encrypt(docNumber) ?: docNumber,
        issuingCountry = cryptoManager.encrypt(issuingCountry) ?: issuingCountry
    )
}

fun UserProfileEntity.decrypted(cryptoManager: CryptoManager = CryptoManager.getInstance()): UserProfileEntity {
    return this.copy(
        fullName = cryptoManager.decrypt(fullName) ?: fullName,
        dob = cryptoManager.decrypt(dob) ?: dob,
        residency = cryptoManager.decrypt(residency) ?: residency,
        passportNumber = cryptoManager.decrypt(passportNumber) ?: passportNumber,
        licenseNumber = cryptoManager.decrypt(licenseNumber) ?: licenseNumber,
        docType = cryptoManager.decrypt(docType) ?: docType,
        docNumber = cryptoManager.decrypt(docNumber) ?: docNumber,
        issuingCountry = cryptoManager.decrypt(issuingCountry) ?: issuingCountry
    )
}

fun DocumentEntity.encrypted(cryptoManager: CryptoManager = CryptoManager.getInstance()): DocumentEntity {
    return this.copy(
        name = cryptoManager.encrypt(name) ?: name,
        url = cryptoManager.encrypt(url) ?: url,
        docType = cryptoManager.encrypt(docType) ?: docType
    )
}

fun DocumentEntity.decrypted(cryptoManager: CryptoManager = CryptoManager.getInstance()): DocumentEntity {
    return this.copy(
        name = cryptoManager.decrypt(name) ?: name,
        url = cryptoManager.decrypt(url) ?: url,
        docType = cryptoManager.decrypt(docType) ?: docType
    )
}
