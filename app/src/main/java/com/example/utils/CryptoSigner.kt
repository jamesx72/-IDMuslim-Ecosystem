package com.example.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature

/**
 * Handles generating ECDSA cryptographic signatures using the hardware-backed Android KeyStore.
 */
object CryptoSigner {
    private const val KEY_ALIAS = "IDMuslimDocumentSigner"

    /**
     * Signs a given string payload using a locally generated ECDSA private key.
     * Generates the key pair if it does not already exist in the KeyStore.
     */
    fun signPayload(payload: String): String? {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"
                )
                val parameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                ).setDigests(KeyProperties.DIGEST_SHA256).build()
                keyPairGenerator.initialize(parameterSpec)
                keyPairGenerator.generateKeyPair()
            }

            val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign(privateKey)
            signature.update(payload.toByteArray(Charsets.UTF_8))
            val signatureBytes = signature.sign()

            return Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
