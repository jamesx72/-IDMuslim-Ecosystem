package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class ExtractedIdCredentials(
    val fullName: String = "",
    val dateOfBirth: String = "",
    val docNumber: String = "",
    val docType: String = "",
    val issuingCountry: String = "",
    val expiryDate: String = "",
    val rawText: String = "",
    val confidenceConfidenceScore: Float = 0.0f
)

object DocumentOcrProcessor {

    private const val TAG = "DocumentOcrProcessor"
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractCredentialsFromImageUri(context: Context, imageUri: Uri): ExtractedIdCredentials {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val result = parseVisionText(visionText.text)
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "OCR recognition failure", exception)
                        continuation.resume(ExtractedIdCredentials(rawText = "Erreur OCR: ${exception.localizedMessage}"))
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error preparing image for OCR", e)
                continuation.resume(ExtractedIdCredentials(rawText = "Erreur fichier: ${e.localizedMessage}"))
            }
        }
    }

    suspend fun extractCredentialsFromBitmap(bitmap: Bitmap): ExtractedIdCredentials {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val result = parseVisionText(visionText.text)
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "OCR bitmap failure", exception)
                        continuation.resume(ExtractedIdCredentials(rawText = "Erreur OCR: ${exception.localizedMessage}"))
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing bitmap for OCR", e)
                continuation.resume(ExtractedIdCredentials(rawText = "Erreur: ${e.localizedMessage}"))
            }
        }
    }

    fun parseVisionText(rawText: String): ExtractedIdCredentials {
        if (rawText.isBlank()) return ExtractedIdCredentials()

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        var detectedName = ""
        var detectedDob = ""
        var detectedDocNumber = ""
        var detectedDocType = "National ID"
        var detectedCountry = ""
        var detectedExpiry = ""

        // Check for MRZ (Machine Readable Zone in Passports / IDs)
        val mrzLines = lines.filter { it.contains("<<") || (it.length > 25 && it.count { c -> c == '<' } >= 2) }
        if (mrzLines.isNotEmpty()) {
            detectedDocType = if (mrzLines.any { it.startsWith("P<") || it.startsWith("P") }) "Passport" else "National ID"

            for (mrz in mrzLines) {
                if (mrz.startsWith("P<")) {
                    // Standard passport MRZ line 1: P<FRAGUYON<<JEAN<LUC<<<<<<<<<<<<<<<<<<
                    val countryCode = mrz.substring(2, 5.coerceAtMost(mrz.length)).replace("<", "")
                    if (countryCode.isNotBlank()) detectedCountry = countryCode
                    val namePart = mrz.substring(5.coerceAtMost(mrz.length)).trim()
                    val splitNames = namePart.split("<<")
                    if (splitNames.isNotEmpty()) {
                        val surname = splitNames[0].replace("<", " ").trim()
                        val givenNames = if (splitNames.size > 1) splitNames[1].replace("<", " ").trim() else ""
                        detectedName = "$givenNames $surname".trim()
                    }
                }
                // Line 2 often contains doc number and DOB (YYMMDD)
                val mrzClean = mrz.replace("<", " ").trim()
                val docNumMatch = Pattern.compile("([A-Z0-9]{8,10})").matcher(mrzClean)
                if (docNumMatch.find() && detectedDocNumber.isEmpty()) {
                    detectedDocNumber = docNumMatch.group(1) ?: ""
                }
            }
        }

        // Regex patterns for standard textual documents
        val dobPattern = Pattern.compile("(?:N[eé]\\(?e?\\)?\\s*le|Date of birth|DOB|D\\.O\\.B|Né\\(e\\)\\s*le|Born on)[:\\s]*([0-9]{2}[/.\\-][0-9]{2}[/.\\-][0-9]{4}|[0-9]{2}\\s+[A-Za-z]{3,9}\\s+[0-9]{4})", Pattern.CASE_INSENSITIVE)
        val expiryPattern = Pattern.compile("(?:Expire|Expiration|Expiry|Valid until|Valable jusqu'au|Date d'expiration)[:\\s]*([0-9]{2}[/.\\-][0-9]{2}[/.\\-][0-9]{4})", Pattern.CASE_INSENSITIVE)
        val namePattern = Pattern.compile("(?:Nom|Name|Nom / Surname|Full Name|Prénom|Given names?)[:\\s]+([A-Za-zÀ-ÿ\\-\\s]{3,35})", Pattern.CASE_INSENSITIVE)
        val docNumPattern = Pattern.compile("(?:N°|No\\.?|Document No|ID No|Passport No|Passeport n°)[:\\s]*([A-Z0-9\\-]{6,14})", Pattern.CASE_INSENSITIVE)
        val genericDatePattern = Pattern.compile("\\b([0-3]?[0-9][/.\\-][0-1]?[0-9][/.\\-](?:19|20)[0-9]{2})\\b")

        for (line in lines) {
            // DOB detection
            if (detectedDob.isEmpty()) {
                val dobMatcher = dobPattern.matcher(line)
                if (dobMatcher.find()) {
                    detectedDob = dobMatcher.group(1)?.replace("-", "/")?.replace(".", "/") ?: ""
                }
            }

            // Expiry date detection
            if (detectedExpiry.isEmpty()) {
                val expMatcher = expiryPattern.matcher(line)
                if (expMatcher.find()) {
                    detectedExpiry = expMatcher.group(1)?.replace("-", "/")?.replace(".", "/") ?: ""
                }
            }

            // Name detection
            if (detectedName.isEmpty()) {
                val nameMatcher = namePattern.matcher(line)
                if (nameMatcher.find()) {
                    val potentialName = nameMatcher.group(1)?.trim() ?: ""
                    if (potentialName.isNotBlank() && !potentialName.equals("ID", ignoreCase = true) && !potentialName.equals("CARD", ignoreCase = true)) {
                        detectedName = potentialName
                    }
                }
            }

            // Doc Number detection
            if (detectedDocNumber.isEmpty()) {
                val numMatcher = docNumPattern.matcher(line)
                if (numMatcher.find()) {
                    detectedDocNumber = numMatcher.group(1)?.trim() ?: ""
                }
            }

            // Country detection
            if (detectedCountry.isEmpty()) {
                when {
                    line.contains("FRANCAISE", ignoreCase = true) || line.contains("FRANCE", ignoreCase = true) -> detectedCountry = "France"
                    line.contains("BELGIQUE", ignoreCase = true) || line.contains("BELGIUM", ignoreCase = true) -> detectedCountry = "Belgique"
                    line.contains("CANADA", ignoreCase = true) -> detectedCountry = "Canada"
                    line.contains("MAROC", ignoreCase = true) || line.contains("MOROCCO", ignoreCase = true) -> detectedCountry = "Maroc"
                    line.contains("ALGERIE", ignoreCase = true) || line.contains("ALGERIA", ignoreCase = true) -> detectedCountry = "Algérie"
                    line.contains("TUNISIE", ignoreCase = true) || line.contains("TUNISIA", ignoreCase = true) -> detectedCountry = "Tunisie"
                    line.contains("SENEGAL", ignoreCase = true) -> detectedCountry = "Sénégal"
                    line.contains("COTE D'IVOIRE", ignoreCase = true) || line.contains("CÔTE D'IVOIRE", ignoreCase = true) -> detectedCountry = "Côte d'Ivoire"
                    line.contains("UNITED KINGDOM", ignoreCase = true) || line.contains("BRITISH", ignoreCase = true) -> detectedCountry = "United Kingdom"
                    line.contains("UNITED STATES", ignoreCase = true) || line.contains("USA", ignoreCase = true) -> detectedCountry = "USA"
                }
            }

            // Document type heuristics
            if (line.contains("PASSEPORT", ignoreCase = true) || line.contains("PASSPORT", ignoreCase = true)) {
                detectedDocType = "Passport"
            } else if (line.contains("CARTE NATIONALE", ignoreCase = true) || line.contains("IDENTITY CARD", ignoreCase = true) || line.contains("IDENTITE", ignoreCase = true)) {
                detectedDocType = "National ID"
            } else if (line.contains("PERMIS DE CONDUIRE", ignoreCase = true) || line.contains("DRIVING LICENCE", ignoreCase = true) || line.contains("DRIVER LICENSE", ignoreCase = true)) {
                detectedDocType = "Driver License"
            }
        }

        // Fallback for DOB / Dates if specific label not found
        if (detectedDob.isEmpty()) {
            val dateMatcher = genericDatePattern.matcher(rawText)
            if (dateMatcher.find()) {
                detectedDob = dateMatcher.group(1) ?: ""
                if (dateMatcher.find() && detectedExpiry.isEmpty()) {
                    detectedExpiry = dateMatcher.group(1) ?: ""
                }
            }
        }

        // Clean detected name
        detectedName = detectedName.split(" ")
            .filter { it.isNotBlank() && it.length > 1 }
            .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

        return ExtractedIdCredentials(
            fullName = detectedName,
            dateOfBirth = detectedDob,
            docNumber = detectedDocNumber,
            docType = detectedDocType,
            issuingCountry = detectedCountry,
            expiryDate = detectedExpiry,
            rawText = rawText,
            confidenceConfidenceScore = if (detectedName.isNotBlank() || detectedDocNumber.isNotBlank()) 0.92f else 0.5f
        )
    }
}
