package com.example.utils

import com.example.security.EccCryptoEngine
import com.example.security.KeystoreManager

/**
 * High-level ECDSA P-256 (SHA256withECDSA) cryptographic signer delegating to [KeystoreManager]
 * and [EccCryptoEngine] for hardware-backed identity & document signatures.
 */
object CryptoSigner {

    fun signPayload(payload: String): String? {
        return KeystoreManager.signData(payload)
    }

    fun verifyPayload(payload: String, signatureBase64: String): Boolean {
        return KeystoreManager.verifyData(payload, signatureBase64)
    }

    fun getPublicKeyBase64(): String? {
        return KeystoreManager.getPublicKeyBase64()
    }

    fun signDocumentDigest(docId: String, plaintextBytes: ByteArray, ownerUid: String, timestamp: Long): String? {
        val digestHex = EccCryptoEngine.sha256Hex(plaintextBytes)
        val manifest = "$docId|$digestHex|$ownerUid|$timestamp"
        return KeystoreManager.signData(manifest)
    }
}
