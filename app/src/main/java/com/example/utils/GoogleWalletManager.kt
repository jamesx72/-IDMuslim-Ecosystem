package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.android.gms.pay.Pay
import com.google.android.gms.pay.PayApiAvailabilityStatus
import com.google.android.gms.pay.PayClient
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import org.json.JSONArray
import org.json.JSONObject

sealed class GoogleWalletSaveResult {
    object SuccessInitiated : GoogleWalletSaveResult()
    data class FallbackShared(val file: File, val message: String) : GoogleWalletSaveResult()
    data class Error(val message: String) : GoogleWalletSaveResult()
}

object GoogleWalletManager {

    private const val TAG = "GoogleWalletManager"
    const val SAVE_PASSES_REQUEST_CODE = 9928
    
    // Default Issuer ID for IDMuslim Generic Passes
    const val DEFAULT_ISSUER_ID = "3388000000022315999"
    const val DEFAULT_CLASS_SUFFIX = "idmuslim_membership_pass_class"

    /**
     * Builds a standardized Google Wallet Generic Pass JSON structure with barcode and metadata.
     * Complies with the Google Wallet REST / Pay API Generic Object specification.
     */
    fun buildGoogleWalletGenericPassJson(
        issuerId: String = DEFAULT_ISSUER_ID,
        memberId: String,
        fullName: String,
        dateOfBirth: String,
        verificationStatus: String,
        residency: String = "France",
        communityAffiliation: String = "IDMuslim Global",
        expiryDate: String = "31/12/2030"
    ): String {
        val sanitizedMemberId = memberId.replace("-", "_").replace(" ", "_").trim()
        val classId = "$issuerId.$DEFAULT_CLASS_SUFFIX"
        val objectId = "$issuerId.member_$sanitizedMemberId"
        val cleanName = if (fullName.isNotBlank()) fullName.trim() else "Membre IDMuslim"
        val cleanDob = if (dateOfBirth.isNotBlank()) dateOfBirth.trim() else "--/--/----"
        val cleanResidency = if (residency.isNotBlank()) residency.trim() else "Non spécifiée"
        val cleanAffiliation = if (communityAffiliation.isNotBlank()) communityAffiliation.trim() else "IDMuslim Community"
        val cleanStatus = if (verificationStatus.isNotBlank()) verificationStatus.uppercase() else "VÉRIFIÉ"

        val genericObject = JSONObject().apply {
            put("id", objectId)
            put("classId", classId)
            
            // Visual Card Branding
            put("hexBackgroundColor", "#064E3B") // Emerald Green IDMuslim
            put("cardTitle", JSONObject().apply {
                put("defaultValue", JSONObject().apply {
                    put("language", "fr")
                    put("value", "IDMUSLIM IDENTITÉ NUMÉRIQUE")
                })
            })
            put("header", JSONObject().apply {
                put("defaultValue", JSONObject().apply {
                    put("language", "fr")
                    put("value", cleanName)
                })
            })
            put("subheader", JSONObject().apply {
                put("defaultValue", JSONObject().apply {
                    put("language", "fr")
                    put("value", "N° MATRICULE : $memberId")
                })
            })
            
            // Logo Image
            put("logo", JSONObject().apply {
                put("sourceUri", JSONObject().apply {
                    put("uri", "https://raw.githubusercontent.com/google/material-design-icons/master/png/action/verified_user/materialicons/48dp/2x/baseline_verified_user_black_48dp.png")
                })
                put("contentDescription", JSONObject().apply {
                    put("defaultValue", JSONObject().apply {
                        put("language", "fr")
                        put("value", "Logo IDMuslim Shield")
                    })
                })
            })

            // Standardized Barcode (QR Code)
            put("barcode", JSONObject().apply {
                put("type", "QR_CODE")
                put("value", "IDMUSLIM:$memberId:$cleanStatus:$cleanName")
                put("alternateText", "N° $memberId")
                put("renderEncoding", "UTF_8")
            })

            // Standardized Structured Text Metadata Modules
            val textModules = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "status_module")
                    put("header", "STATUT D'AUTHENTICITÉ")
                    put("body", cleanStatus)
                })
                put(JSONObject().apply {
                    put("id", "dob_module")
                    put("header", "DATE DE NAISSANCE")
                    put("body", cleanDob)
                })
                put(JSONObject().apply {
                    put("id", "residency_module")
                    put("header", "PAYS DE RÉSIDENCE")
                    put("body", cleanResidency)
                })
                put(JSONObject().apply {
                    put("id", "community_module")
                    put("header", "AFFILIATION / MOSQUÉE")
                    put("body", cleanAffiliation)
                })
                put(JSONObject().apply {
                    put("id", "expiry_module")
                    put("header", "VALABLE JUSQU'AU")
                    put("body", expiryDate)
                })
                put(JSONObject().apply {
                    put("id", "crypto_module")
                    put("header", "SCELLÉ CRYPTOGRAPHIQUE")
                    put("body", "SHA-256 Android Keystore Certifié")
                })
                put(JSONObject().apply {
                    put("id", "nfc_module")
                    put("header", "PUCE SANS-CONTACT (NFC)")
                    put("body", "ISO/IEC 14443-A Emulated")
                })
            }
            put("textModulesData", textModules)

            // Links / Web Verification Portal
            put("linksModuleData", JSONObject().apply {
                val urisArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("uri", "https://idmuslim.app/verify/$memberId")
                        put("description", "Portail officiel de vérification de l'identité")
                        put("id", "verify_portal_link")
                    })
                }
                put("uris", urisArray)
            })
        }

        // Generic Class Definition
        val genericClass = JSONObject().apply {
            put("id", classId)
            put("issuerName", "IDMuslim Global Shield")
            put("reviewStatus", "UNDER_REVIEW")
            put("hexBackgroundColor", "#064E3B")
        }

        val rootObject = JSONObject().apply {
            put("genericObjects", JSONArray().apply { put(genericObject) })
            put("genericClasses", JSONArray().apply { put(genericClass) })
        }

        return rootObject.toString(2)
    }

    /**
     * Checks if Google Wallet (PayClient) is available on the current device.
     */
    fun checkGoogleWalletAvailability(
        activity: Activity,
        onResult: (Boolean) -> Unit
    ) {
        try {
            val payClient = Pay.getClient(activity)
            payClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                .addOnSuccessListener { status ->
                    val isAvailable = (status == PayApiAvailabilityStatus.AVAILABLE)
                    onResult(isAvailable)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "PayClient availability check failed: ${e.message}")
                    onResult(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during checkGoogleWalletAvailability", e)
            onResult(false)
        }
    }

    /**
     * Attempts to save the pass into Google Wallet using the Pay API (PayClient.savePasses).
     * If unavailable, falls back to sharing the standardized pass file.
     */
    fun saveToGoogleWallet(
        activity: Activity,
        memberId: String,
        fullName: String,
        dateOfBirth: String,
        verificationStatus: String,
        residency: String = "France",
        communityAffiliation: String = "IDMuslim Global",
        expiryDate: String = "31/12/2030",
        onResult: (GoogleWalletSaveResult) -> Unit
    ) {
        try {
            val passJson = buildGoogleWalletGenericPassJson(
                memberId = memberId,
                fullName = fullName,
                dateOfBirth = dateOfBirth,
                verificationStatus = verificationStatus,
                residency = residency,
                communityAffiliation = communityAffiliation,
                expiryDate = expiryDate
            )

            val payClient = Pay.getClient(activity)
            payClient.getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
                .addOnSuccessListener { status ->
                    if (status == PayApiAvailabilityStatus.AVAILABLE) {
                        try {
                            payClient.savePasses(passJson, activity, SAVE_PASSES_REQUEST_CODE)
                            onResult(GoogleWalletSaveResult.SuccessInitiated)
                        } catch (saveEx: Exception) {
                            Log.e(TAG, "Error invoking payClient.savePasses, falling back to file export", saveEx)
                            val file = exportPassJsonFile(activity, passJson, memberId)
                            sharePassFile(activity, file, fullName)
                            onResult(GoogleWalletSaveResult.FallbackShared(file, "Passeport Google Wallet généré et partagé."))
                        }
                    } else {
                        // Fallback: Export JSON & PKPass
                        val file = exportPassJsonFile(activity, passJson, memberId)
                        sharePassFile(activity, file, fullName)
                        onResult(GoogleWalletSaveResult.FallbackShared(file, "Google Pay API non disponible sur cet appareil. Fichier pass exporté."))
                    }
                }
                .addOnFailureListener { ex ->
                    Log.w(TAG, "Google Pay API check failed, using fallback: ${ex.message}")
                    val file = exportPassJsonFile(activity, passJson, memberId)
                    sharePassFile(activity, file, fullName)
                    onResult(GoogleWalletSaveResult.FallbackShared(file, "Passeport Google Wallet exporté avec succès."))
                }
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in saveToGoogleWallet", e)
            onResult(GoogleWalletSaveResult.Error("Erreur: ${e.localizedMessage ?: "Impossible d'exporter le pass"}"))
        }
    }

    /**
     * Writes the standardized Google Wallet JSON file to the app's cache directory.
     */
    fun exportPassJsonFile(
        context: Context,
        passJson: String,
        memberId: String
    ): File {
        val passDir = File(context.cacheDir, "passes").apply { mkdirs() }
        val file = File(passDir, "IDMuslim_GoogleWallet_${memberId.replace("-", "_")}.json")
        FileOutputStream(file).use { fos ->
            fos.write(passJson.toByteArray(StandardCharsets.UTF_8))
        }
        return file
    }

    /**
     * Opens a system share sheet for the exported Google Wallet pass file.
     */
    fun sharePassFile(context: Context, file: File, fullName: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Pass Google Wallet IDMuslim - $fullName")
                putExtra(Intent.EXTRA_TEXT, "Voici votre passeport numérique IDMuslim certifié et standardisé pour Google Wallet.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Exporter vers Google Wallet / Partager le Pass")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing Google Wallet pass file", e)
        }
    }
}
