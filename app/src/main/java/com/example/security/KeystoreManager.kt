package com.example.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Hardware-backed & NIST P-256 (secp256r1) ECC Manager for IDMuslim.
 *
 * Manages:
 * 1. Hardware AndroidKeyStore ECDSA P-256 signing keypair (`id_muslim_digital_signature_key`).
 * 2. Vault ECIES P-256 keypair (private key wrapped at rest by an AndroidKeyStore AES-256-GCM
 *    wrapper key, ensuring compatibility across API 24..36 without hardcoded fallback secrets).
 */
object KeystoreManager {
    private const val TAG = "KeystoreManager"
    private const val KEY_ALIAS = "id_muslim_digital_signature_key"
    private const val ECIES_WRAPPER_ALIAS = "id_muslim_ecies_aes_wrapper_v1"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val PREFS_NAME = "idmuslim_ecc_keystore_prefs"
    private const val KEY_WRAPPED_ECIES_PRIV = "wrapped_ecies_priv_p256_v1"
    private const val KEY_ECIES_PUB = "ecies_pub_p256_v1"
    private const val AES_GCM = "AES/GCM/NoPadding"
    private const val GCM_IV_LEN = 12
    private const val GCM_TAG_BITS = 128

    @Volatile
    private var inMemoryProcessEciesKeyPair: KeyPair? = null

    @Volatile
    private var inMemoryProcessSigningKeyPair: KeyPair? = null

    fun generateKeyIfNotExists() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC,
                    ANDROID_KEYSTORE
                )
                val parameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                )
                    .setAlgorithmParameterSpec(ECGenParameterSpec(EccCryptoEngine.CURVE_NAME))
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setUserAuthenticationRequired(false)
                    .build()

                keyPairGenerator.initialize(parameterSpec)
                keyPairGenerator.generateKeyPair()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "AndroidKeyStore ECDSA unavailable in runtime (${e.javaClass.simpleName}); using ephemeral process P-256 keypair.")
            if (inMemoryProcessSigningKeyPair == null) {
                synchronized(this) {
                    if (inMemoryProcessSigningKeyPair == null) {
                        inMemoryProcessSigningKeyPair = EccCryptoEngine.generateP256KeyPair()
                    }
                }
            }
        }
    }

    fun signData(data: String): String? {
        return signBytes(data.toByteArray(Charsets.UTF_8))
    }

    fun signBytes(payload: ByteArray): String? {
        return try {
            generateKeyIfNotExists()
            val privateKey = getSigningPrivateKey() ?: return null
            val signature = Signature.getInstance(EccCryptoEngine.SIGNATURE_ALGORITHM)
            signature.initSign(privateKey)
            signature.update(payload)
            val signatureBytes = signature.sign()
            Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
        } catch (e: Throwable) {
            Log.e(TAG, "ECDSA signing failed: ${e.javaClass.simpleName}")
            null
        }
    }

    fun verifyData(data: String, signatureBase64: String): Boolean {
        return verifyBytes(data.toByteArray(Charsets.UTF_8), signatureBase64)
    }

    fun verifyBytes(payload: ByteArray, signatureBase64: String): Boolean {
        return try {
            val publicKey = getSigningPublicKey() ?: return false
            val signatureBytes = Base64.decode(signatureBase64.trim(), Base64.NO_WRAP)
            EccCryptoEngine.verifyEcdsa(publicKey, payload, signatureBytes)
        } catch (e: Throwable) {
            Log.e(TAG, "ECDSA verification failed: ${e.javaClass.simpleName}")
            false
        }
    }

    fun verifyWithPublicKeyBase64(data: String, signatureBase64: String, publicKeyBase64: String): Boolean {
        val pubKey = EccCryptoEngine.decodePublicKeyBase64(publicKeyBase64).getOrNull() ?: return false
        return EccCryptoEngine.verifyEcdsaBase64(pubKey, data.toByteArray(Charsets.UTF_8), signatureBase64)
    }

    fun getPublicKeyBase64(): String? {
        return try {
            generateKeyIfNotExists()
            val publicKey = getSigningPublicKey() ?: return null
            Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
        } catch (e: Throwable) {
            Log.e(TAG, "Unable to read ECDSA public key: ${e.javaClass.simpleName}")
            null
        }
    }

    fun getSigningKeyFingerprintSha256(): String {
        val pub = getSigningPublicKey() ?: return "PENDING-KEYSTORE"
        return EccCryptoEngine.publicKeyFingerprintSha256(pub)
    }

    // -------------------------------------------------------------------------
    // Vault ECIES (ECDH P-256 + HKDF-SHA256 + AES-256-GCM) Keypair Management
    // -------------------------------------------------------------------------

    @Synchronized
    fun getOrCreateVaultEciesKeyPair(context: Context): KeyPair {
        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val wrappedPrivB64 = prefs.getString(KEY_WRAPPED_ECIES_PRIV, null)
        val pubB64 = prefs.getString(KEY_ECIES_PUB, null)

        val wrapperKey = getOrCreateKeystoreAesWrapperKey()
        if (wrapperKey != null && !wrappedPrivB64.isNullOrBlank() && !pubB64.isNullOrBlank()) {
            val unwrappedPriv = unwrapPrivateKey(wrapperKey, wrappedPrivB64)
            val decodedPub = EccCryptoEngine.decodePublicKeyBase64(pubB64).getOrNull()
            if (unwrappedPriv != null && decodedPub != null) {
                return KeyPair(decodedPub, unwrappedPriv)
            }
        }

        if (wrapperKey != null) {
            val freshPair = EccCryptoEngine.generateP256KeyPair()
            val privBytes = freshPair.private.encoded
            try {
                val wrappedB64 = wrapBytesWithKeystoreKey(wrapperKey, privBytes)
                val encodedPubB64 = EccCryptoEngine.encodePublicKeyBase64(freshPair.public)
                prefs.edit()
                    .putString(KEY_WRAPPED_ECIES_PRIV, wrappedB64)
                    .putString(KEY_ECIES_PUB, encodedPubB64)
                    .commit()
                return freshPair
            } finally {
                Arrays.fill(privBytes, 0.toByte())
            }
        }

        // Ephemeral process-scoped P-256 keypair when AndroidKeyStore is unavailable (e.g., JVM Robolectric).
        return inMemoryProcessEciesKeyPair ?: EccCryptoEngine.generateP256KeyPair().also {
            inMemoryProcessEciesKeyPair = it
        }
    }

    fun encryptBytesForVault(
        context: Context,
        plaintext: ByteArray,
        associatedData: ByteArray = ByteArray(0)
    ): Result<ByteArray> {
        val kp = getOrCreateVaultEciesKeyPair(context)
        return EccCryptoEngine.encryptBytesToWireFormat(plaintext, kp.public, associatedData)
    }

    fun decryptBytesFromVault(
        context: Context,
        wireBytes: ByteArray,
        associatedData: ByteArray = ByteArray(0)
    ): Result<ByteArray> {
        val kp = getOrCreateVaultEciesKeyPair(context)
        return EccCryptoEngine.decryptBytesFromWireFormat(wireBytes, kp.private, kp.public, associatedData)
    }

    fun getVaultEciesPublicKeyBase64(context: Context): String {
        val kp = getOrCreateVaultEciesKeyPair(context)
        return EccCryptoEngine.encodePublicKeyBase64(kp.public)
    }

    fun getVaultEciesFingerprintSha256(context: Context): String {
        val kp = getOrCreateVaultEciesKeyPair(context)
        return EccCryptoEngine.publicKeyFingerprintSha256(kp.public)
    }

    private fun getSigningPrivateKey(): PrivateKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            (keyStore.getKey(KEY_ALIAS, null) as? PrivateKey) ?: inMemoryProcessSigningKeyPair?.private
        } catch (_: Throwable) {
            inMemoryProcessSigningKeyPair?.private
        }
    }

    private fun getSigningPublicKey(): PublicKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            keyStore.getCertificate(KEY_ALIAS)?.publicKey ?: inMemoryProcessSigningKeyPair?.public
        } catch (_: Throwable) {
            inMemoryProcessSigningKeyPair?.public
        }
    }

    private fun getOrCreateKeystoreAesWrapperKey(): SecretKey? {
        return try {
            val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!ks.containsAlias(ECIES_WRAPPER_ALIAS)) {
                val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val spec = KeyGenParameterSpec.Builder(
                    ECIES_WRAPPER_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                kg.init(spec)
                kg.generateKey()
            }
            (ks.getEntry(ECIES_WRAPPER_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
        } catch (e: Throwable) {
            Log.w(TAG, "AndroidKeyStore AES wrapper unavailable (${e.javaClass.simpleName})")
            null
        }
    }

    private fun wrapBytesWithKeystoreKey(secretKey: SecretKey, plainBytes: ByteArray): String {
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv ?: ByteArray(GCM_IV_LEN).also { SecureRandom().nextBytes(it) }
        val enc = cipher.doFinal(plainBytes)
        val combined = ByteArray(iv.size + enc.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(enc, 0, combined, iv.size, enc.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun unwrapPrivateKey(secretKey: SecretKey, wrappedBase64: String): PrivateKey? {
        val decrypted = ByteArray(0)
        return try {
            val combined = Base64.decode(wrappedBase64, Base64.NO_WRAP)
            if (combined.size <= GCM_IV_LEN + 16) return null
            val iv = combined.copyOfRange(0, GCM_IV_LEN)
            val cipherBytes = combined.copyOfRange(GCM_IV_LEN, combined.size)
            val cipher = Cipher.getInstance(AES_GCM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
            val pkcs8 = cipher.doFinal(cipherBytes)
            try {
                EccCryptoEngine.decodePrivateKeyBytes(pkcs8).getOrNull()
            } finally {
                Arrays.fill(pkcs8, 0.toByte())
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed unwrapping vault ECIES key: ${e.javaClass.simpleName}")
            null
        } finally {
            Arrays.fill(decrypted, 0.toByte())
        }
    }
}
