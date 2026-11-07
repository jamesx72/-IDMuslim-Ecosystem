package com.example.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Hardware-backed key manager for the SQLCipher Digital Wallet database.
 * Generates a 256-bit (64-hex-char) passphrase and wraps it at rest using
 * AndroidKeyStore AES-256-GCM via [CryptoManager].
 */
object WalletVaultKeyManager {
    private const val PREFS_NAME = "idmuslim_sqlcipher_wallet_prefs"
    private const val KEY_WRAPPED_PASSPHRASE = "wrapped_sqlcipher_passphrase_v1"
    private const val KEY_ROTATED_AT = "sqlcipher_key_rotated_at"
    private const val KEY_VAULT_PIN_HASH = "wallet_pin_sha256"
    private const val KEY_VAULT_PIN_SALT = "wallet_pin_salt"

    @Synchronized
    fun getPassphraseHex(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val crypto = CryptoManager.getInstance()
        val storedEncrypted = prefs.getString(KEY_WRAPPED_PASSPHRASE, null)

        if (!storedEncrypted.isNullOrBlank()) {
            val decrypted = crypto.decrypt(storedEncrypted)
            if (!decrypted.isNullOrBlank() && decrypted.length >= 64) {
                return decrypted
            }
        }

        val freshHex = generateRandom256BitHex()
        val wrapped = crypto.encrypt(freshHex) ?: freshHex
        val now = System.currentTimeMillis()
        prefs.edit()
            .putString(KEY_WRAPPED_PASSPHRASE, wrapped)
            .putLong(KEY_ROTATED_AT, now)
            .commit()
        return freshHex
    }

    /**
     * Returns a fresh UTF-8 byte array copy suitable for [net.sqlcipher.database.SupportFactory].
     */
    fun getPassphraseBytes(context: Context): ByteArray {
        return getPassphraseHex(context).toByteArray(Charsets.UTF_8)
    }

    /**
     * Generates a brand-new 256-bit passphrase, persists it wrapped under AndroidKeyStore,
     * and returns the new hex string for SQLCipher PRAGMA rekey.
     */
    @Synchronized
    fun commitRotatedPassphraseHex(context: Context, newHex: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val crypto = CryptoManager.getInstance()
        val wrapped = crypto.encrypt(newHex) ?: newHex
        prefs.edit()
            .putString(KEY_WRAPPED_PASSPHRASE, wrapped)
            .putLong(KEY_ROTATED_AT, System.currentTimeMillis())
            .commit()
    }

    fun generateRandom256BitHex(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getKeyFingerprintSha256(context: Context): String {
        val hex = getPassphraseHex(context)
        val digest = MessageDigest.getInstance("SHA-256").digest(hex.toByteArray(Charsets.UTF_8))
        return digest.take(12).joinToString(":") { "%02X".format(it) }
    }

    fun getKeyLastRotatedAt(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ts = prefs.getLong(KEY_ROTATED_AT, 0L)
        return if (ts > 0L) ts else System.currentTimeMillis()
    }

    fun hasCustomVaultPin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return !prefs.getString(KEY_VAULT_PIN_HASH, null).isNullOrBlank()
    }

    fun setCustomVaultPin(context: Context, pin: String) {
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val hash = hashPin(pin, salt)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_VAULT_PIN_SALT, salt)
            .putString(KEY_VAULT_PIN_HASH, hash)
            .apply()
    }

    fun verifyCustomVaultPin(context: Context, pin: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val salt = prefs.getString(KEY_VAULT_PIN_SALT, null) ?: return false
        val expectedHash = prefs.getString(KEY_VAULT_PIN_HASH, null) ?: return false
        return MessageDigest.isEqual(
            expectedHash.toByteArray(Charsets.UTF_8),
            hashPin(pin, salt).toByteArray(Charsets.UTF_8)
        )
    }

    private fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }
}
