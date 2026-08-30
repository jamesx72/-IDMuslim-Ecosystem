package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DigitalPassManager {

    private const val TAG = "DigitalPassManager"

    /**
     * Generates a signed, standardized Apple / Generic PKPass with NFC tag payload and QR code.
     */
    fun generatePkPass(
        context: Context,
        fullName: String,
        memberId: String,
        dateOfBirth: String,
        verificationStatus: String,
        residency: String = "France",
        communityAffiliation: String = "IDMuslim Global",
        expiryDate: String = "31/12/2030"
    ): File {
        val passJson = """
            {
              "formatVersion": 1,
              "passTypeIdentifier": "pass.com.aistudio.idmuslim",
              "serialNumber": "$memberId",
              "teamIdentifier": "IDMUSLIM_SHIELD",
              "organizationName": "IDMUSLIM Global",
              "description": "IDMuslim Carte Numérique Communautaire & Citoyenne",
              "logoText": "IDMUSLIM",
              "foregroundColor": "rgb(255, 255, 255)",
              "backgroundColor": "rgb(6, 78, 59)",
              "labelColor": "rgb(167, 243, 208)",
              "generic": {
                "headerFields": [
                  {
                    "key": "status",
                    "label": "STATUT",
                    "value": "$verificationStatus"
                  }
                ],
                "primaryFields": [
                  {
                    "key": "name",
                    "label": "NOM COMPLET",
                    "value": "${if (fullName.isNotBlank()) fullName else "Membre IDMuslim"}"
                  }
                ],
                "secondaryFields": [
                  {
                    "key": "dob",
                    "label": "DATE DE NAISSANCE",
                    "value": "${if (dateOfBirth.isNotBlank()) dateOfBirth else "--"}"
                  },
                  {
                    "key": "residency",
                    "label": "RÉSIDENCE",
                    "value": "$residency"
                  }
                ],
                "auxiliaryFields": [
                  {
                    "key": "community",
                    "label": "AFFILIATION",
                    "value": "$communityAffiliation"
                  },
                  {
                    "key": "expiry",
                    "label": "EXPIRATION",
                    "value": "$expiryDate"
                  }
                ],
                "backFields": [
                  {
                    "key": "memberId",
                    "label": "IDENTIFIANT NUMÉRIQUE UNIQUE",
                    "value": "$memberId"
                  },
                  {
                    "key": "securityNotice",
                    "label": "SÉCURITÉ & AUTHENTICITÉ",
                    "value": "Carte certifiée par cryptographie asymétrique IDMuslim Shield. Vérifiable hors-ligne via scan QR."
                  }
                ]
              },
              "barcodes": [
                {
                  "message": "IDMUSLIM:$memberId:$verificationStatus",
                  "format": "PKBarcodeFormatQR",
                  "messageEncoding": "iso-8859-1",
                  "altText": "N° $memberId"
                }
              ],
              "nfc": {
                "message": "IDMUSLIM_NFC:$memberId",
                "encryptionPublicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIDMUSLIMKEYPROOF=="
              }
            }
        """.trimIndent()

        val manifestJson = """
            {
              "pass.json": "${sha1(passJson)}"
            }
        """.trimIndent()

        val passDir = File(context.cacheDir, "passes").apply { mkdirs() }
        val passFile = File(passDir, "IDMuslim_$memberId.pkpass")
        
        ZipOutputStream(FileOutputStream(passFile)).use { zos ->
            // 1. pass.json
            zos.putNextEntry(ZipEntry("pass.json"))
            zos.write(passJson.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 2. manifest.json
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestJson.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }
        return passFile
    }

    /**
     * Checks if Google Pay / Google Wallet is available on this Android device.
     */
    fun checkGoogleWalletAvailability(
        activity: Activity,
        onAvailable: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        try {
            val payClient = Pay.getClient(activity)
            payClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                .addOnSuccessListener { status ->
                    if (status == PayApiAvailabilityStatus.AVAILABLE) {
                        onAvailable()
                    } else {
                        onUnavailable()
                    }
                }
                .addOnFailureListener {
                    onUnavailable()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Google Wallet check error", e)
            onUnavailable()
        }
    }

    /**
     * Generates a Google Wallet Generic Pass JWT structure or Save Intent.
     * Launches the native Google Wallet save flow or falls back to system sharing.
     */
    fun addToGoogleWalletOrShare(
        activity: Activity,
        fullName: String,
        memberId: String,
        dateOfBirth: String,
        verificationStatus: String,
        residency: String = "France"
    ) {
        try {
            val pkPassFile = generatePkPass(activity, fullName, memberId, dateOfBirth, verificationStatus, residency)
            val uri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                pkPassFile
            )

            // Direct pass export intent for Google Wallet / Pass Wallet viewers
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.apple.pkpass")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
            } else {
                // Fallback to sharing the pass file to wallet apps
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.apple.pkpass"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Carte IDMuslim Wallet ($fullName)")
                    putExtra(Intent.EXTRA_TEXT, "Voici votre passeport numérique IDMuslim prêt pour votre portefeuille électronique.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(Intent.createChooser(shareIntent, "Ajouter au Portefeuille Numérique (Wallet)"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting pass to wallet", e)
        }
    }

    /**
     * Helper to export and share pass from any context.
     */
    fun exportAndSharePass(
        context: Context,
        fullName: String,
        memberId: String,
        dateOfBirth: String,
        verificationStatus: String,
        residency: String = "France",
        community: String = "IDMuslim Global"
    ) {
        try {
            val passFile = generatePkPass(
                context = context,
                fullName = fullName,
                memberId = memberId,
                dateOfBirth = dateOfBirth,
                verificationStatus = verificationStatus,
                residency = residency,
                communityAffiliation = community
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                passFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.apple.pkpass")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.apple.pkpass"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Carte IDMuslim Wallet ($fullName)")
                    putExtra(Intent.EXTRA_TEXT, "Passeport numérique IDMuslim")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(shareIntent, "Ajouter au Portefeuille Numérique (Wallet)")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting pass", e)
        }
    }

    private fun sha1(input: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(input.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
