package com.example.utils

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object PdfEncryptor {
    
    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            PDFBoxResourceLoader.init(context)
            isInitialized = true
        }
    }

    /**
     * Applies standard PDF password protection using PDFBox.
     */
    fun encryptPdf(context: Context, pdfBytes: ByteArray, userPassword: String): ByteArray {
        if (userPassword.isEmpty()) return pdfBytes
        
        init(context)

        try {
            val document = PDDocument.load(ByteArrayInputStream(pdfBytes))
            val accessPermission = AccessPermission()
            
            // We use the same password for both owner and user for simplicity
            val spp = StandardProtectionPolicy(userPassword, userPassword, accessPermission)
            spp.encryptionKeyLength = 128
            
            document.protect(spp)
            
            val outStream = ByteArrayOutputStream()
            document.save(outStream)
            document.close()
            
            return outStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return pdfBytes // Fallback to unencrypted if it fails
        }
    }
}
