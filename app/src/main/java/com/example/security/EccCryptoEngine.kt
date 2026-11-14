package com.example.security

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.AlgorithmParameters
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptographically sound NIST P-256 (secp256r1) Elliptic Curve Cryptography engine.
 *
 * Implements:
 * - Strict P-256 curve & public-point validation (anti invalid-curve & point-at-infinity attacks).
 * - ECDSA SHA-256 signing & verification with ASN.1 DER <-> IEEE P1363 conversion and low-S validation.
 * - Authenticated ECIES hybrid encryption: Ephemeral-Static ECDH (secp256r1) + RFC 5869 HKDF-SHA256 + AES-256-GCM.
 * - Binary envelope serialization ("IDM1" wire format) for encrypted documents/blobs.
 * - Explicit zeroization of intermediate key material in memory.
 */
object EccCryptoEngine {

    const val CURVE_NAME = "secp256r1"
    const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    const val KEY_AGREEMENT_ALGORITHM = "ECDH"
    const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"

    private const val AES_KEY_SIZE_BYTES = 32 // AES-256
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val MAX_EPHEMERAL_SPKI_BYTES = 256
    private const val ENVELOPE_VERSION: Byte = 0x01
    private val ENVELOPE_MAGIC = byteArrayOf('I'.code.toByte(), 'D'.code.toByte(), 'M'.code.toByte(), '1'.code.toByte())
    private val HKDF_CONTEXT_INFO = "IDMuslim-ECIES-P256-HKDF-SHA256-AES256GCM-v1".toByteArray(Charsets.UTF_8)

    // NIST P-256 (secp256r1) domain constants for explicit mathematical curve validation
    private val P256_P = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16)
    private val P256_A = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16)
    private val P256_B = BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16)
    private val P256_N = BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16)
    private val P256_HALF_N = P256_N.shiftRight(1)

    private val secureRandom: SecureRandom by lazy { SecureRandom() }

    data class EciesEnvelope(
        val version: Byte,
        val ephemeralPublicKeySpki: ByteArray,
        val iv: ByteArray,
        val ciphertextWithTag: ByteArray
    ) {
        fun serialize(): ByteArray {
            require(ephemeralPublicKeySpki.size in 1..MAX_EPHEMERAL_SPKI_BYTES) {
                "Invalid ephemeral public key length"
            }
            require(iv.size == GCM_IV_LENGTH_BYTES) { "Invalid GCM IV length" }
            require(ciphertextWithTag.size >= 16) { "Ciphertext missing authentication tag" }

            val totalSize = ENVELOPE_MAGIC.size + 1 + 2 + ephemeralPublicKeySpki.size + 1 + iv.size + ciphertextWithTag.size
            val buffer = ByteBuffer.allocate(totalSize)
            buffer.put(ENVELOPE_MAGIC)
            buffer.put(version)
            buffer.putShort(ephemeralPublicKeySpki.size.toShort())
            buffer.put(ephemeralPublicKeySpki)
            buffer.put(iv.size.toByte())
            buffer.put(iv)
            buffer.put(ciphertextWithTag)
            return buffer.array()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EciesEnvelope) return false
            return version == other.version &&
                ephemeralPublicKeySpki.contentEquals(other.ephemeralPublicKeySpki) &&
                iv.contentEquals(other.iv) &&
                ciphertextWithTag.contentEquals(other.ciphertextWithTag)
        }

        override fun hashCode(): Int {
            var result = version.toInt()
            result = 31 * result + ephemeralPublicKeySpki.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            result = 31 * result + ciphertextWithTag.contentHashCode()
            return result
        }

        companion object {
            fun deserialize(bytes: ByteArray): Result<EciesEnvelope> = runCatching {
                val minSize = ENVELOPE_MAGIC.size + 1 + 2 + 1 + GCM_IV_LENGTH_BYTES + 16
                require(bytes.size >= minSize) { "Encrypted envelope is truncated" }

                val buffer = ByteBuffer.wrap(bytes)
                val magic = ByteArray(ENVELOPE_MAGIC.size)
                buffer.get(magic)
                require(magic.contentEquals(ENVELOPE_MAGIC)) { "Invalid ECIES envelope magic header" }

                val version = buffer.get()
                require(version == ENVELOPE_VERSION) { "Unsupported ECIES envelope version: $version" }

                val spkiLen = buffer.short.toInt() and 0xFFFF
                require(spkiLen in 32..MAX_EPHEMERAL_SPKI_BYTES) { "Invalid ephemeral public key length: $spkiLen" }
                require(buffer.remaining() > spkiLen + 1) { "Corrupt ECIES envelope structure" }
                val spki = ByteArray(spkiLen)
                buffer.get(spki)

                val ivLen = buffer.get().toInt() and 0xFF
                require(ivLen == GCM_IV_LENGTH_BYTES) { "Invalid GCM IV length: $ivLen" }
                require(buffer.remaining() >= ivLen + 16) { "Truncated GCM payload" }
                val iv = ByteArray(ivLen)
                buffer.get(iv)

                val ciphertext = ByteArray(buffer.remaining())
                buffer.get(ciphertext)

                EciesEnvelope(
                    version = version,
                    ephemeralPublicKeySpki = spki,
                    iv = iv,
                    ciphertextWithTag = ciphertext
                )
            }
        }
    }

    /**
     * Generates a fresh NIST P-256 (secp256r1) software EC KeyPair suitable for ECDH and ECDSA.
     */
    fun generateP256KeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec(CURVE_NAME), secureRandom)
        return kpg.generateKeyPair()
    }

    /**
     * Validates that an [ECPublicKey] lies strictly on NIST P-256 (secp256r1),
     * is not the point at infinity, and satisfies the curve equation y^2 = x^3 + ax + b (mod p).
     */
    fun validateP256PublicKey(publicKey: PublicKey): ECPublicKey {
        if (publicKey !is ECPublicKey) {
            throw GeneralSecurityException("Key is not an ECPublicKey")
        }
        val params = publicKey.params ?: throw GeneralSecurityException("ECPublicKey missing domain parameters")
        if (params.order != P256_N || params.cofactor != 1) {
            throw GeneralSecurityException("ECPublicKey does not use NIST P-256 (secp256r1)")
        }
        val point = publicKey.w
        if (point == null || point == ECPoint.POINT_INFINITY) {
            throw GeneralSecurityException("ECPublicKey is point at infinity")
        }
        val x = point.affineX
        val y = point.affineY
        if (x < BigInteger.ZERO || x >= P256_P || y < BigInteger.ZERO || y >= P256_P) {
            throw GeneralSecurityException("ECPublicKey coordinates out of field range")
        }
        val lhs = y.modPow(BigInteger.valueOf(2), P256_P)
        val rhs = x.modPow(BigInteger.valueOf(3), P256_P)
            .add(P256_A.multiply(x))
            .add(P256_B)
            .mod(P256_P)
        if (lhs != rhs) {
            throw GeneralSecurityException("ECPublicKey does not satisfy P-256 curve equation")
        }
        return publicKey
    }

    fun getP256Spec(): ECParameterSpec {
        val params = AlgorithmParameters.getInstance("EC")
        params.init(ECGenParameterSpec(CURVE_NAME))
        return params.getParameterSpec(ECParameterSpec::class.java)
    }

    fun encodePublicKeyBase64(publicKey: PublicKey): String {
        val validated = validateP256PublicKey(publicKey)
        return Base64Codec.encode(validated.encoded)
    }

    fun decodePublicKeyBase64(spkiBase64: String): Result<ECPublicKey> = runCatching {
        val bytes = Base64Codec.decode(spkiBase64.trim())
        decodePublicKeyBytes(bytes).getOrThrow()
    }

    fun decodePublicKeyBytes(spkiBytes: ByteArray): Result<ECPublicKey> = runCatching {
        val kf = KeyFactory.getInstance("EC")
        val pub = kf.generatePublic(X509EncodedKeySpec(spkiBytes))
        validateP256PublicKey(pub)
    }

    fun decodePrivateKeyBytes(pkcs8Bytes: ByteArray): Result<PrivateKey> = runCatching {
        val kf = KeyFactory.getInstance("EC")
        kf.generatePrivate(PKCS8EncodedKeySpec(pkcs8Bytes))
    }

    /**
     * Computes a human-readable SHA-256 fingerprint of an EC public key's X.509 SPKI encoding.
     */
    fun publicKeyFingerprintSha256(publicKey: PublicKey): String {
        val digest = sha256(publicKey.encoded)
        return digest.take(12).joinToString(":") { "%02X".format(it) }
    }

    fun sha256(data: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(data)

    fun sha256Hex(data: ByteArray): String = sha256(data).joinToString("") { "%02x".format(it) }

    // -------------------------------------------------------------------------
    // ECDSA (SHA256withECDSA) + Low-S & IEEE P1363 Utilities
    // -------------------------------------------------------------------------

    fun signEcdsa(privateKey: PrivateKey, payload: ByteArray): Result<ByteArray> = runCatching {
        val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
        sig.initSign(privateKey, secureRandom)
        sig.update(payload)
        sig.sign()
    }

    fun signEcdsaBase64(privateKey: PrivateKey, payload: ByteArray, enforceLowS: Boolean = false): Result<String> =
        signEcdsa(privateKey, payload).mapCatching { derSig ->
            val finalSig = if (enforceLowS) normalizeDerSignatureLowS(derSig).getOrThrow() else derSig
            Base64Codec.encode(finalSig)
        }

    fun verifyEcdsa(
        publicKey: PublicKey,
        payload: ByteArray,
        derSignature: ByteArray,
        requireLowS: Boolean = false
    ): Boolean {
        return try {
            val validatedPub = validateP256PublicKey(publicKey)
            val (r, s) = parseDerSignature(derSignature).getOrNull() ?: return false
            if (r <= BigInteger.ZERO || r >= P256_N || s <= BigInteger.ZERO || s >= P256_N) {
                return false
            }
            if (requireLowS && s > P256_HALF_N) {
                return false
            }
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
            sig.initVerify(validatedPub)
            sig.update(payload)
            sig.verify(derSignature)
        } catch (_: Exception) {
            false
        }
    }

    fun verifyEcdsaBase64(
        publicKey: PublicKey,
        payload: ByteArray,
        signatureBase64: String,
        requireLowS: Boolean = false
    ): Boolean {
        return try {
            val sigBytes = Base64Codec.decode(signatureBase64.trim())
            verifyEcdsa(publicKey, payload, sigBytes, requireLowS)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Normalizes an ASN.1 DER ECDSA signature into canonical low-S form (s <= n/2)
     * to prevent ECDSA signature malleability.
     */
    fun normalizeDerSignatureLowS(derSignature: ByteArray): Result<ByteArray> = runCatching {
        val (r, s) = parseDerSignature(derSignature).getOrThrow()
        val canonicalS = if (s > P256_HALF_N) P256_N.subtract(s) else s
        encodeDerSignature(r, canonicalS)
    }

    /**
     * Converts ASN.1 DER ECDSA signature to fixed 64-byte IEEE P1363 format (r[32] || s[32]).
     */
    fun derToP1363(derSignature: ByteArray): Result<ByteArray> = runCatching {
        val (r, s) = parseDerSignature(derSignature).getOrThrow()
        val out = ByteArray(64)
        writeUnsignedFixed32(r, out, 0)
        writeUnsignedFixed32(s, out, 32)
        out
    }

    /**
     * Converts a 64-byte IEEE P1363 (r || s) ECDSA signature into ASN.1 DER format.
     */
    fun p1363ToDer(p1363: ByteArray): Result<ByteArray> = runCatching {
        require(p1363.size == 64) { "Expected 64-byte P1363 signature for P-256" }
        val r = BigInteger(1, p1363.copyOfRange(0, 32))
        val s = BigInteger(1, p1363.copyOfRange(32, 64))
        require(r > BigInteger.ZERO && r < P256_N) { "ECDSA r scalar out of range" }
        require(s > BigInteger.ZERO && s < P256_N) { "ECDSA s scalar out of range" }
        encodeDerSignature(r, s)
    }

    private fun writeUnsignedFixed32(value: BigInteger, dest: ByteArray, offset: Int) {
        val raw = value.toByteArray()
        val trimmed = if (raw.size > 32 && raw[0] == 0.toByte()) {
            raw.copyOfRange(raw.size - 32, raw.size)
        } else {
            raw
        }
        require(trimmed.size <= 32) { "Scalar exceeds 32 bytes" }
        System.arraycopy(trimmed, 0, dest, offset + (32 - trimmed.size), trimmed.size)
    }

    private fun parseDerSignature(der: ByteArray): Result<Pair<BigInteger, BigInteger>> = runCatching {
        require(der.size >= 8 && der[0] == 0x30.toByte()) { "Invalid ASN.1 SEQUENCE" }
        var idx = 1
        val seqLenByte = der[idx++].toInt() and 0xFF
        val seqLen = if ((seqLenByte and 0x80) != 0) {
            val count = seqLenByte and 0x7F
            require(count == 1 && idx < der.size) { "Unsupported ASN.1 length encoding" }
            der[idx++].toInt() and 0xFF
        } else {
            seqLenByte
        }
        require(idx + seqLen == der.size) { "ASN.1 sequence length mismatch" }

        require(der[idx++] == 0x02.toByte()) { "Expected INTEGER for r" }
        val rLen = der[idx++].toInt() and 0xFF
        require(rLen in 1..33 && idx + rLen <= der.size) { "Invalid r length" }
        val r = BigInteger(1, der.copyOfRange(idx, idx + rLen))
        idx += rLen

        require(idx < der.size && der[idx++] == 0x02.toByte()) { "Expected INTEGER for s" }
        val sLen = der[idx++].toInt() and 0xFF
        require(sLen in 1..33 && idx + sLen == der.size) { "Invalid s length" }
        val s = BigInteger(1, der.copyOfRange(idx, idx + sLen))

        r to s
    }

    private fun encodeDerSignature(r: BigInteger, s: BigInteger): ByteArray {
        val rBytes = r.toByteArray()
        val sBytes = s.toByteArray()
        val contentLen = 2 + rBytes.size + 2 + sBytes.size
        val out = ByteArrayOutputStream(2 + contentLen)
        out.write(0x30)
        if (contentLen >= 128) {
            out.write(0x81)
        }
        out.write(contentLen)
        out.write(0x02)
        out.write(rBytes.size)
        out.write(rBytes)
        out.write(0x02)
        out.write(sBytes.size)
        out.write(sBytes)
        return out.toByteArray()
    }

    // -------------------------------------------------------------------------
    // ECIES: Ephemeral-Static ECDH (secp256r1) + HKDF-SHA256 + AES-256-GCM
    // -------------------------------------------------------------------------

    /**
     * Encrypts arbitrary bytes (e.g., PDF documents, identity images, or wallet secrets)
     * for [recipientPublicKey] using ECIES (Ephemeral ECDH P-256 + HKDF-SHA256 + AES-256-GCM).
     */
    fun encryptWithEcies(
        plaintext: ByteArray,
        recipientPublicKey: PublicKey,
        associatedData: ByteArray = ByteArray(0)
    ): Result<EciesEnvelope> = runCatching {
        val validatedRecipient = validateP256PublicKey(recipientPublicKey)
        val ephemeralKeyPair = generateP256KeyPair()
        val ephemeralSpki = ephemeralKeyPair.public.encoded
        val recipientSpki = validatedRecipient.encoded

        val sharedSecret = deriveEcdhSecret(ephemeralKeyPair.private, validatedRecipient)
        val aesKeyBytes = ByteArray(AES_KEY_SIZE_BYTES)
        try {
            val info = buildEciesHkdfInfo(ephemeralSpki, recipientSpki)
            val derived = hkdfSha256(
                ikm = sharedSecret,
                salt = sha256(ephemeralSpki),
                info = info,
                outputLength = AES_KEY_SIZE_BYTES
            )
            System.arraycopy(derived, 0, aesKeyBytes, 0, AES_KEY_SIZE_BYTES)
            Arrays.fill(derived, 0.toByte())

            val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(aesKeyBytes, "AES"),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            )
            if (associatedData.isNotEmpty()) {
                cipher.updateAAD(associatedData)
            }
            val ciphertext = cipher.doFinal(plaintext)

            EciesEnvelope(
                version = ENVELOPE_VERSION,
                ephemeralPublicKeySpki = ephemeralSpki,
                iv = iv,
                ciphertextWithTag = ciphertext
            )
        } finally {
            Arrays.fill(sharedSecret, 0.toByte())
            Arrays.fill(aesKeyBytes, 0.toByte())
        }
    }

    /**
     * Encrypts and serializes directly to the self-contained "IDM1" binary envelope format.
     */
    fun encryptBytesToWireFormat(
        plaintext: ByteArray,
        recipientPublicKey: PublicKey,
        associatedData: ByteArray = ByteArray(0)
    ): Result<ByteArray> = encryptWithEcies(plaintext, recipientPublicKey, associatedData)
        .map { it.serialize() }

    /**
     * Decrypts an [EciesEnvelope] using the recipient's P-256 private key and optional [associatedData].
     */
    fun decryptWithEcies(
        envelope: EciesEnvelope,
        recipientPrivateKey: PrivateKey,
        recipientPublicKey: PublicKey,
        associatedData: ByteArray = ByteArray(0)
    ): Result<ByteArray> = runCatching {
        val validatedRecipient = validateP256PublicKey(recipientPublicKey)
        val ephemeralPub = decodePublicKeyBytes(envelope.ephemeralPublicKeySpki).getOrThrow()

        val sharedSecret = deriveEcdhSecret(recipientPrivateKey, ephemeralPub)
        val aesKeyBytes = ByteArray(AES_KEY_SIZE_BYTES)
        try {
            val info = buildEciesHkdfInfo(envelope.ephemeralPublicKeySpki, validatedRecipient.encoded)
            val derived = hkdfSha256(
                ikm = sharedSecret,
                salt = sha256(envelope.ephemeralPublicKeySpki),
                info = info,
                outputLength = AES_KEY_SIZE_BYTES
            )
            System.arraycopy(derived, 0, aesKeyBytes, 0, AES_KEY_SIZE_BYTES)
            Arrays.fill(derived, 0.toByte())

            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(aesKeyBytes, "AES"),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, envelope.iv)
            )
            if (associatedData.isNotEmpty()) {
                cipher.updateAAD(associatedData)
            }
            cipher.doFinal(envelope.ciphertextWithTag)
        } finally {
            Arrays.fill(sharedSecret, 0.toByte())
            Arrays.fill(aesKeyBytes, 0.toByte())
        }
    }

    /**
     * Parses a serialized "IDM1" binary blob and decrypts it via ECIES.
     */
    fun decryptBytesFromWireFormat(
        wireBytes: ByteArray,
        recipientPrivateKey: PrivateKey,
        recipientPublicKey: PublicKey,
        associatedData: ByteArray = ByteArray(0)
    ): Result<ByteArray> = EciesEnvelope.deserialize(wireBytes).fold(
        onSuccess = { env -> decryptWithEcies(env, recipientPrivateKey, recipientPublicKey, associatedData) },
        onFailure = { err -> Result.failure(err) }
    )

    fun isValidEciesWireFormat(wireBytes: ByteArray): Boolean = EciesEnvelope.deserialize(wireBytes).isSuccess

    /**
     * Performs ECDH key agreement and verifies the shared secret is non-zero.
     */
    fun deriveEcdhSecret(privateKey: PrivateKey, peerPublicKey: ECPublicKey): ByteArray {
        val ka = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM)
        ka.init(privateKey)
        ka.doPhase(peerPublicKey, true)
        val secret = ka.generateSecret()
        if (secret.isEmpty() || secret.all { it == 0.toByte() }) {
            Arrays.fill(secret, 0.toByte())
            throw GeneralSecurityException("ECDH produced degenerate shared secret")
        }
        return secret
    }

    /**
     * RFC 5869 HKDF-SHA256 Extract + Expand.
     */
    fun hkdfSha256(
        ikm: ByteArray,
        salt: ByteArray,
        info: ByteArray,
        outputLength: Int
    ): ByteArray {
        require(outputLength in 1..(255 * 32)) { "Invalid HKDF output length" }
        val effectiveSalt = if (salt.isEmpty()) ByteArray(32) else salt

        val extractMac = Mac.getInstance("HmacSHA256")
        extractMac.init(SecretKeySpec(effectiveSalt, "HmacSHA256"))
        val prk = extractMac.doFinal(ikm)

        val okm = ByteArray(outputLength)
        var previousBlock = ByteArray(0)
        var offset = 0
        var counter = 1
        try {
            while (offset < outputLength) {
                val expandMac = Mac.getInstance("HmacSHA256")
                expandMac.init(SecretKeySpec(prk, "HmacSHA256"))
                expandMac.update(previousBlock)
                expandMac.update(info)
                expandMac.update(counter.toByte())
                val block = expandMac.doFinal()
                val toCopy = minOf(block.size, outputLength - offset)
                System.arraycopy(block, 0, okm, offset, toCopy)
                offset += toCopy
                Arrays.fill(previousBlock, 0.toByte())
                previousBlock = block
                counter++
            }
            return okm
        } finally {
            Arrays.fill(prk, 0.toByte())
            Arrays.fill(previousBlock, 0.toByte())
        }
    }

    private fun buildEciesHkdfInfo(ephemeralSpki: ByteArray, recipientSpki: ByteArray): ByteArray {
        val out = ByteArrayOutputStream(HKDF_CONTEXT_INFO.size + ephemeralSpki.size + recipientSpki.size + 4)
        out.write(HKDF_CONTEXT_INFO)
        out.write(ephemeralSpki.size and 0xFF)
        out.write(ephemeralSpki)
        out.write(recipientSpki.size and 0xFF)
        out.write(recipientSpki)
        return out.toByteArray()
    }

    /**
     * API-24+ and Pure-JVM compatible RFC 4648 Base64 codec (no dependency on java.util.Base64 API 26+).
     */
    object Base64Codec {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        private val DECODE_TABLE = IntArray(256) { -1 }.apply {
            for (i in ALPHABET.indices) {
                this[ALPHABET[i].code] = i
            }
        }

        fun encode(input: ByteArray): String {
            if (input.isEmpty()) return ""
            val sb = StringBuilder(((input.size + 2) / 3) * 4)
            var i = 0
            while (i < input.size) {
                val b0 = input[i].toInt() and 0xFF
                val hasB1 = i + 1 < input.size
                val hasB2 = i + 2 < input.size
                val b1 = if (hasB1) input[i + 1].toInt() and 0xFF else 0
                val b2 = if (hasB2) input[i + 2].toInt() and 0xFF else 0

                val triple = (b0 shl 16) or (b1 shl 8) or b2
                sb.append(ALPHABET[(triple ushr 18) and 0x3F])
                sb.append(ALPHABET[(triple ushr 12) and 0x3F])
                sb.append(if (hasB1) ALPHABET[(triple ushr 6) and 0x3F] else '=')
                sb.append(if (hasB2) ALPHABET[triple and 0x3F] else '=')
                i += 3
            }
            return sb.toString()
        }

        fun decode(input: String): ByteArray {
            val cleaned = input.filterNot { it.isWhitespace() }
            if (cleaned.isEmpty()) return ByteArray(0)
            require(cleaned.length % 4 == 0) { "Invalid Base64 input length" }

            var padding = 0
            if (cleaned.endsWith("==")) padding = 2 else if (cleaned.endsWith("=")) padding = 1
            val outLen = (cleaned.length / 4) * 3 - padding
            val out = ByteArray(outLen)
            var outIdx = 0
            var i = 0
            while (i < cleaned.length) {
                val c0 = decodeChar(cleaned[i])
                val c1 = decodeChar(cleaned[i + 1])
                val c2 = if (cleaned[i + 2] == '=') 0 else decodeChar(cleaned[i + 2])
                val c3 = if (cleaned[i + 3] == '=') 0 else decodeChar(cleaned[i + 3])
                val triple = (c0 shl 18) or (c1 shl 12) or (c2 shl 6) or c3

                if (outIdx < outLen) out[outIdx++] = ((triple ushr 16) and 0xFF).toByte()
                if (outIdx < outLen) out[outIdx++] = ((triple ushr 8) and 0xFF).toByte()
                if (outIdx < outLen) out[outIdx++] = (triple and 0xFF).toByte()
                i += 4
            }
            return out
        }

        private fun decodeChar(c: Char): Int {
            val code = c.code
            if (code >= 256 || DECODE_TABLE[code] == -1) {
                throw IllegalArgumentException("Invalid Base64 character: $c")
            }
            return DECODE_TABLE[code]
        }
    }
}
