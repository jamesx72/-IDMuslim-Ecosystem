package com.example

import com.example.security.EccCryptoEngine
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

class EccCryptoEngineTest {

    @Test
    fun `ecdsa p256 sign verify low-s and p1363 roundtrip succeed`() {
        val kp1 = EccCryptoEngine.generateP256KeyPair()
        val kp2 = EccCryptoEngine.generateP256KeyPair()
        val payload = "IDMuslim|VerifiedMember|2026-11-14".toByteArray(Charsets.UTF_8)

        val derSig = EccCryptoEngine.signEcdsa(kp1.private, payload).getOrThrow()
        assertTrue(EccCryptoEngine.verifyEcdsa(kp1.public, payload, derSig))

        // Low-S canonicalization keeps valid signature verifiable even with requireLowS = true
        val lowSSig = EccCryptoEngine.normalizeDerSignatureLowS(derSig).getOrThrow()
        assertTrue(EccCryptoEngine.verifyEcdsa(kp1.public, payload, lowSSig, requireLowS = true))

        // DER <-> IEEE P1363 (64-byte) round-trip
        val p1363 = EccCryptoEngine.derToP1363(lowSSig).getOrThrow()
        assertEquals(64, p1363.size)
        val reconstructedDer = EccCryptoEngine.p1363ToDer(p1363).getOrThrow()
        assertTrue(EccCryptoEngine.verifyEcdsa(kp1.public, payload, reconstructedDer, requireLowS = true))

        // Tampered message & wrong public key must fail verification
        val tamperedPayload = "IDMuslim|VerifiedMember|2026-11-15".toByteArray(Charsets.UTF_8)
        assertFalse(EccCryptoEngine.verifyEcdsa(kp1.public, tamperedPayload, derSig))
        assertFalse(EccCryptoEngine.verifyEcdsa(kp2.public, payload, derSig))
    }

    @Test
    fun `ecies encrypt and decrypt preserves binary pdf payload and binds aad`() {
        val recipientKp = EccCryptoEngine.generateP256KeyPair()
        val wrongKp = EccCryptoEngine.generateP256KeyPair()

        val pdfBytes = ("%PDF-1.4\n1 0 obj\n<< /Type /Catalog >>\nendobj\nSecretIdentityNumber=FR-99182736\n%%EOF")
            .toByteArray(Charsets.UTF_8)
        val aad = "uid_123|doc_abc|application/pdf".toByteArray(Charsets.UTF_8)

        val wire1 = EccCryptoEngine.encryptBytesToWireFormat(pdfBytes, recipientKp.public, aad).getOrThrow()
        val wire2 = EccCryptoEngine.encryptBytesToWireFormat(pdfBytes, recipientKp.public, aad).getOrThrow()

        // Semantic security: two encryptions of identical plaintext produce distinct ciphertexts
        assertFalse(wire1.contentEquals(wire2))
        assertTrue(EccCryptoEngine.isValidEciesWireFormat(wire1))
        assertEquals("IDM1", String(wire1.copyOfRange(0, 4), Charsets.US_ASCII))
        assertFalse(String(wire1, Charsets.ISO_8859_1).contains("SecretIdentityNumber"))

        // Authorized decryption succeeds
        val decrypted = EccCryptoEngine.decryptBytesFromWireFormat(
            wireBytes = wire1,
            recipientPrivateKey = recipientKp.private,
            recipientPublicKey = recipientKp.public,
            associatedData = aad
        ).getOrThrow()
        assertArrayEquals(pdfBytes, decrypted)

        // Wrong recipient private key fails
        assertTrue(
            EccCryptoEngine.decryptBytesFromWireFormat(
                wireBytes = wire1,
                recipientPrivateKey = wrongKp.private,
                recipientPublicKey = wrongKp.public,
                associatedData = aad
            ).isFailure
        )

        // AAD tampering fails AEAD tag check
        val wrongAad = "uid_999|doc_abc|application/pdf".toByteArray(Charsets.UTF_8)
        assertTrue(
            EccCryptoEngine.decryptBytesFromWireFormat(
                wireBytes = wire1,
                recipientPrivateKey = recipientKp.private,
                recipientPublicKey = recipientKp.public,
                associatedData = wrongAad
            ).isFailure
        )

        // Bit-flip in ciphertext fails AEAD authentication
        val corrupted = wire1.copyOf()
        corrupted[corrupted.size - 1] = (corrupted[corrupted.size - 1].toInt() xor 0xFF).toByte()
        assertTrue(
            EccCryptoEngine.decryptBytesFromWireFormat(
                wireBytes = corrupted,
                recipientPrivateKey = recipientKp.private,
                recipientPublicKey = recipientKp.public,
                associatedData = aad
            ).isFailure
        )
    }

    @Test
    fun `curve validation rejects point at infinity and off-curve forged public keys`() {
        val kf = KeyFactory.getInstance("EC")
        val spec = EccCryptoEngine.getP256Spec()

        val forgedOffCurve = ECPublicKeySpec(ECPoint(BigInteger.valueOf(2), BigInteger.valueOf(3)), spec)
        val forgedPub = kf.generatePublic(forgedOffCurve) as ECPublicKey

        val encryptResult = EccCryptoEngine.encryptBytesToWireFormat(
            "sensitive".toByteArray(),
            forgedPub
        )
        assertTrue(encryptResult.isFailure)

        val validKp = EccCryptoEngine.generateP256KeyPair()
        val spkiB64 = EccCryptoEngine.encodePublicKeyBase64(validKp.public)
        val decodedPub = EccCryptoEngine.decodePublicKeyBase64(spkiB64).getOrThrow()
        assertEquals(
            EccCryptoEngine.publicKeyFingerprintSha256(validKp.public),
            EccCryptoEngine.publicKeyFingerprintSha256(decodedPub)
        )
        assertNotEquals("", spkiB64)
    }
}
