package com.example.security

import android.content.Context
import java.security.SecureRandom
import android.util.Base64

object DatabaseKeyManager {
    fun getPassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences("secure_db_prefs", Context.MODE_PRIVATE)
        val encryptedKey = prefs.getString("db_key", null)
        
        val crypto = CryptoManager.getInstance()
        
        if (encryptedKey != null) {
            val decryptedStr = crypto.decrypt(encryptedKey)
            if (decryptedStr != null && decryptedStr.isNotEmpty()) {
                try {
                    return Base64.decode(decryptedStr, Base64.NO_WRAP)
                } catch (e: Exception) {
                    // fallthrough to generate new key
                }
            }
        }
        
        // Generate new 32-byte key
        val newKey = ByteArray(32)
        SecureRandom().nextBytes(newKey)
        
        val newKeyBase64 = Base64.encodeToString(newKey, Base64.NO_WRAP)
        val encryptedNewKey = crypto.encrypt(newKeyBase64)
        
        prefs.edit().putString("db_key", encryptedNewKey).apply()
        
        return newKey
    }
}
