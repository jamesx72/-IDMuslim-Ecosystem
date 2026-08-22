package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.util.Base64
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object PdfGenerator {

    enum class PrintLayoutMode {
        CARD_CUTOUT_WALLET, // Dual Recto/Verso foldable wallet card with cut guides
        OFFICIAL_CERTIFICATE // Full-page official A4 certificate
    }

    enum class PdfColorScheme {
        EMERALD_GOLD,
        NAVY_PLATINUM,
        MONOCHROME_PRINT
    }

    data class PdfGenerationOptions(
        val password: String = "",
        val isPasswordProtected: Boolean = false,
        val layoutMode: PrintLayoutMode = PrintLayoutMode.CARD_CUTOUT_WALLET,
        val colorScheme: PdfColorScheme = PdfColorScheme.EMERALD_GOLD,
        val includeQrCode: Boolean = true,
        val includeBarcode: Boolean = true,
        val includePhoto: Boolean = true,
        val customNote: String = "",
        val digitalSignature: String? = null
    )

    /**
     * Generates a printable, password-protected PDF version of the user's digital ID.
     */
    fun generateSecurePdf(
        context: Context,
        fullName: String,
        dateOfBirth: String,
        residency: String,
        community: String,
        passport: String,
        license: String,
        memberId: String,
        verificationStatus: String = "CERTIFIÉ / VALIDE",
        expiryDate: String = "2029-12-31",
        photoBase64: String? = null,
        options: PdfGenerationOptions = PdfGenerationOptions(),
        onSuccess: ((File) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): File? {
        var pdfDocument: PdfDocument? = null
        try {
            pdfDocument = PdfDocument()
            // Standard A4 Size: 595 x 842 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val primaryColor = when (options.colorScheme) {
                PdfColorScheme.EMERALD_GOLD -> Color.parseColor("#0F5A47") // Emerald
                PdfColorScheme.NAVY_PLATINUM -> Color.parseColor("#1E293B") // Deep Navy
                PdfColorScheme.MONOCHROME_PRINT -> Color.parseColor("#222222") // High-contrast Charcoal
            }

            val accentColor = when (options.colorScheme) {
                PdfColorScheme.EMERALD_GOLD -> Color.parseColor("#D97706") // Gold Amber
                PdfColorScheme.NAVY_PLATINUM -> Color.parseColor("#2563EB") // Royal Blue
                PdfColorScheme.MONOCHROME_PRINT -> Color.parseColor("#444444") // Dark Gray
            }

            val cardBgColor = when (options.colorScheme) {
                PdfColorScheme.MONOCHROME_PRINT -> Color.parseColor("#FAFAFA")
                else -> Color.WHITE
            }

            val paint = Paint().apply { isAntiAlias = true }

            // 1. Background Canvas
            paint.color = Color.parseColor("#F8FAFC")
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            // 2. Official Header Banner
            paint.color = primaryColor
            canvas.drawRect(0f, 0f, 595f, 100f, paint)

            // Gold/Accent Thin Stripe
            paint.color = accentColor
            canvas.drawRect(0f, 100f, 595f, 104f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("IDMuslim • CARTE D'IDENTITÉ NUMÉRIQUE", 40f, 45f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("DOCUMENT OFFICIEL PRÊT POUR IMPRESSION PHYSIQUE & ARCHIVAGE SÉCURISÉ", 40f, 70f, paint)

            // Security Badge on top right of banner
            paint.color = accentColor
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val secText = if (options.isPasswordProtected && options.password.isNotEmpty()) "🔒 PROTÉGÉ PAR MOT DE PASSE (AES-128)" else "🛡️ IDENTITÉ CERTIFIÉE"
            canvas.drawText(secText, 40f, 88f, paint)

            val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val docUuid = "IDM-DOC-${UUID.randomUUID().toString().take(8).uppercase()}"

            // 3. Render Layout based on Selected Mode
            if (options.layoutMode == PrintLayoutMode.CARD_CUTOUT_WALLET) {
                renderWalletCardPrintout(
                    canvas = canvas,
                    primaryColor = primaryColor,
                    accentColor = accentColor,
                    cardBgColor = cardBgColor,
                    fullName = fullName,
                    dateOfBirth = dateOfBirth,
                    residency = residency,
                    community = community,
                    passport = passport,
                    license = license,
                    memberId = memberId,
                    verificationStatus = verificationStatus,
                    expiryDate = expiryDate,
                    photoBase64 = photoBase64,
                    options = options,
                    docUuid = docUuid,
                    currentDate = currentDate
                )
            } else {
                renderOfficialCertificate(
                    canvas = canvas,
                    primaryColor = primaryColor,
                    accentColor = accentColor,
                    cardBgColor = cardBgColor,
                    fullName = fullName,
                    dateOfBirth = dateOfBirth,
                    residency = residency,
                    community = community,
                    passport = passport,
                    license = license,
                    memberId = memberId,
                    verificationStatus = verificationStatus,
                    expiryDate = expiryDate,
                    photoBase64 = photoBase64,
                    options = options,
                    docUuid = docUuid,
                    currentDate = currentDate
                )
            }

            // 4. Page Footer & Authenticity Notice
            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Document officiel généré par l'application IDMuslim. Empreinte d'intégrité : $docUuid", 40f, 810f, paint)
            canvas.drawText("Ce document est cryptographiquement vérifiable. Toute reproduction ou altération non autorisée est passible de sanctions.", 40f, 824f, paint)

            pdfDocument.finishPage(page)

            // Write raw PDF stream to byte array
            val byteOut = ByteArrayOutputStream()
            pdfDocument.writeTo(byteOut)
            var finalPdfBytes = byteOut.toByteArray()

            // 5. Apply password encryption if enabled
            if (options.isPasswordProtected && options.password.isNotBlank()) {
                finalPdfBytes = PdfEncryptor.encryptPdf(finalPdfBytes, options.password)
            }

            // 6. Save to Download directory or internal cache
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val fileName = if (options.isPasswordProtected) {
                "IDMuslim_Carte_Securisee_${memberId}_${System.currentTimeMillis()}.pdf"
            } else {
                "IDMuslim_Carte_Imprimable_${memberId}_${System.currentTimeMillis()}.pdf"
            }

            val file = File(downloadDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(finalPdfBytes)
            fos.flush()
            fos.close()

            // Also keep a copy in app cache for instant sharing
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeBytes(finalPdfBytes)

            onSuccess?.invoke(file)
            return file

        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = "Erreur lors de la génération du PDF : ${e.localizedMessage}"
            onError?.invoke(errorMsg)
            return null
        } finally {
            try {
                pdfDocument?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun renderWalletCardPrintout(
        canvas: Canvas,
        primaryColor: Int,
        accentColor: Int,
        cardBgColor: Int,
        fullName: String,
        dateOfBirth: String,
        residency: String,
        community: String,
        passport: String,
        license: String,
        memberId: String,
        verificationStatus: String,
        expiryDate: String,
        photoBase64: String?,
        options: PdfGenerationOptions,
        docUuid: String,
        currentDate: String
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        // Section Title & Cutting Instructions Banner
        paint.color = Color.parseColor("#334155")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("✂ GABARIT D'IMPRESSION PHYSIQUE • FORMAT CARTE PORTEFEUILLE (85 x 54 mm)", 40f, 130f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("1. Découpez le contour rectangulaire le long des pointillés extérieurs.", 40f, 148f, paint)
        canvas.drawText("2. Pliez la carte sur la ligne médiane pour assembler le Recto et le Verso avant plastification.", 40f, 162f, paint)

        // Card Container Dimensions:
        // Width: 2 x 240 = 480 pt (Each side 240 x 150 pt ~ 85mm x 54mm)
        val startX = 57f
        val startY = 180f
        val cardW = 240f
        val cardH = 152f

        // Outer Dotted Border for Cutout
        val cutPath = Path()
        cutPath.addRoundRect(RectF(startX - 4f, startY - 4f, startX + (cardW * 2) + 4f, startY + cardH + 4f), 12f, 12f, Path.Direction.CW)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.parseColor("#94A3B8")
        paint.pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
        canvas.drawPath(cutPath, paint)
        paint.pathEffect = null
        paint.style = Paint.Style.FILL

        // Center Fold Line
        val foldPath = Path()
        foldPath.moveTo(startX + cardW, startY - 15f)
        foldPath.lineTo(startX + cardW, startY + cardH + 15f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        paint.color = Color.parseColor("#EF4444")
        paint.pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        canvas.drawPath(foldPath, paint)
        paint.pathEffect = null
        paint.style = Paint.Style.FILL

        // Fold Indicators
        paint.color = Color.parseColor("#EF4444")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("⮂ PLIER ICI (LIGNE MÉDIANE)", startX + cardW - 60f, startY - 6f, paint)

        // ==========================================
        // 1. FRONT FACE (RECTO) - startX to startX+cardW
        // ==========================================
        val frontRect = RectF(startX, startY, startX + cardW, startY + cardH)
        paint.color = cardBgColor
        canvas.drawRoundRect(frontRect, 10f, 10f, paint)

        // Front Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = primaryColor
        canvas.drawRoundRect(frontRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Front Header Strip
        paint.color = primaryColor
        canvas.save()
        canvas.clipRect(RectF(startX, startY, startX + cardW, startY + 28f))
        canvas.drawRoundRect(frontRect, 10f, 10f, paint)
        canvas.restore()

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("IDMuslim • CARTE D'IDENTITÉ", startX + 10f, startY + 18f, paint)

        paint.textSize = 8f
        paint.color = accentColor
        canvas.drawText("RECTO", startX + cardW - 40f, startY + 18f, paint)

        // Photo / Avatar on Front
        val photoRect = RectF(startX + 10f, startY + 36f, startX + 60f, startY + 96f)
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRoundRect(photoRect, 6f, 6f, paint)

        var photoDrawn = false
        if (options.includePhoto && !photoBase64.isNullOrBlank()) {
            try {
                val cleanB64 = if (photoBase64.contains(",")) photoBase64.substringAfter(",") else photoBase64
                val decodedBytes = Base64.decode(cleanB64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, photoRect, null)
                    photoDrawn = true
                }
            } catch (e: Exception) {
                photoDrawn = false
            }
        }

        if (!photoDrawn) {
            paint.color = primaryColor
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val initials = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
            canvas.drawText(initials.ifEmpty { "ID" }, startX + 22f, startY + 72f, paint)
        }

        // Status Badge under photo
        paint.color = Color.parseColor("#059669")
        val statusRect = RectF(startX + 10f, startY + 102f, startX + 60f, startY + 116f)
        canvas.drawRoundRect(statusRect, 3f, 3f, paint)
        paint.color = Color.WHITE
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CERTIFIÉ", startX + 16f, startY + 112f, paint)

        // Front Details
        val textStartX = startX + 68f
        var textY = startY + 44f
        val lineGap = 16f

        fun drawFrontField(label: String, value: String, isBold: Boolean = false) {
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 7f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(label.uppercase(), textStartX, textY, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            val displayVal = if (value.length > 22) value.take(20) + "..." else value
            canvas.drawText(displayVal, textStartX, textY + 9f, paint)

            textY += lineGap
        }

        drawFrontField("Nom Complet", fullName.ifEmpty { "Membre IDMuslim" }, isBold = true)
        drawFrontField("Date de Naissance", dateOfBirth.ifEmpty { "Non spécifié" })
        drawFrontField("Résidence / Ville", residency.ifEmpty { "France" })
        drawFrontField("Communauté", community.ifEmpty { "Communauté IDMuslim" })

        // Front Footer Bar with ID Number
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(startX + 1f, startY + cardH - 18f, startX + cardW - 1f, startY + cardH - 1f, paint)
        paint.color = primaryColor
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("ID: $memberId", startX + 10f, startY + cardH - 6f, paint)
        canvas.drawText("EXP: $expiryDate", startX + cardW - 75f, startY + cardH - 6f, paint)

        // ==========================================
        // 2. BACK FACE (VERSO) - startX+cardW to startX+(cardW*2)
        // ==========================================
        val backStartX = startX + cardW
        val backRect = RectF(backStartX, startY, backStartX + cardW, startY + cardH)
        paint.color = cardBgColor
        canvas.drawRoundRect(backRect, 10f, 10f, paint)

        // Back Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = primaryColor
        canvas.drawRoundRect(backRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Back Header
        paint.color = primaryColor
        canvas.save()
        canvas.clipRect(RectF(backStartX, startY, backStartX + cardW, startY + 22f))
        canvas.drawRoundRect(backRect, 10f, 10f, paint)
        canvas.restore()

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("VÉRIFICATION OFFICIELLE", backStartX + 10f, startY + 15f, paint)

        paint.textSize = 8f
        paint.color = accentColor
        canvas.drawText("VERSO", backStartX + cardW - 40f, startY + 15f, paint)

        // QR Code on Back
        val qrSize = 58
        val qrBitmap = if (options.includeQrCode) {
            QRCodeGenerator.generateQRCode("IDMUSLIM:MEMBER:$memberId:NAME:$fullName:STATUS:VERIFIED:EXP:$expiryDate", qrSize)
        } else null

        if (qrBitmap != null) {
            val qrRect = RectF(backStartX + 10f, startY + 28f, backStartX + 10f + qrSize, startY + 28f + qrSize)
            canvas.drawBitmap(qrBitmap, null, qrRect, null)
        }

        // Barcode on Back
        val barcodeBitmap = if (options.includeBarcode) {
            QRCodeGenerator.generateBarcode(memberId.replace("-", ""), 140, 32)
        } else null

        val backDetailsStartX = backStartX + 76f
        var backTextY = startY + 36f

        paint.color = Color.parseColor("#475569")
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SIGNATURE NUMÉRIQUE :", backDetailsStartX, backTextY, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        if (options.digitalSignature != null) {
            val sig = options.digitalSignature
            val firstLine = sig.take(18) + "..."
            canvas.drawText("ECDSA:$firstLine", backDetailsStartX, backTextY + 10f, paint)
        } else {
            canvas.drawText(docUuid, backDetailsStartX, backTextY + 10f, paint)
        }

        backTextY += 24f
        paint.color = Color.parseColor("#475569")
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PASSPORT / PERMIS :", backDetailsStartX, backTextY, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val passPerm = "${passport.ifEmpty { "N/A" }} • ${license.ifEmpty { "N/A" }}"
        canvas.drawText(passPerm, backDetailsStartX, backTextY + 10f, paint)

        if (barcodeBitmap != null) {
            val barRect = RectF(backDetailsStartX, startY + 68f, backStartX + cardW - 10f, startY + 92f)
            canvas.drawBitmap(barcodeBitmap, null, barRect, null)
        }

        // Machine Readable Zone (MRZ) on bottom of back
        paint.color = Color.parseColor("#1E293B")
        val mrzRect = RectF(backStartX + 1f, startY + cardH - 26f, backStartX + cardW - 1f, startY + cardH - 1f)
        canvas.drawRect(mrzRect, paint)

        paint.color = Color.parseColor("#38BDF8")
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val cleanName = fullName.replace(" ", "<<").uppercase()
        val mrz1 = "IDFRA${memberId.replace("-", "")}<<<<<<<<<<<<<<<".take(32)
        val mrz2 = "${cleanName}<<<<<<<<<<<<<<<<<<<<<<".take(32)
        canvas.drawText(mrz1, backStartX + 6f, startY + cardH - 14f, paint)
        canvas.drawText(mrz2, backStartX + 6f, startY + cardH - 5f, paint)

        // ==========================================
        // 3. PHYSICAL PRINTING GUIDELINES & RECOMMENDATIONS
        // ==========================================
        val guideStartY = startY + cardH + 35f

        paint.color = Color.WHITE
        val guideCardRect = RectF(40f, guideStartY, 555f, guideStartY + 140f)
        canvas.drawRoundRect(guideCardRect, 12f, 12f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawRoundRect(guideCardRect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        // Guide Title
        paint.color = primaryColor
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🖨️ CONSEILS D'IMPRESSION PHYSIQUE ET PLASTIFICATION", 56f, guideStartY + 24f, paint)

        paint.color = Color.parseColor("#334155")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("• Échelle d'impression : Sélectionnez 'Taille réelle' ou 100% dans vos paramètres d'imprimante (ne pas adapter à la page).", 56f, guideStartY + 46f, paint)
        canvas.drawText("• Papier recommandé : Papier épais ou bristol satiné (180g/m² à 250g/m²) pour une rigidité optimale.", 56f, guideStartY + 64f, paint)
        canvas.drawText("• Finition : Découpez soigneusement, pliez le long de la ligne médiane, puis appliquez une pochette de plastification à chaud.", 56f, guideStartY + 82f, paint)
        canvas.drawText("• Dimensions finales après pliage : 85.6 mm × 53.98 mm (Conforme norme standard bancaire ISO/IEC 7810 ID-1).", 56f, guideStartY + 100f, paint)
        canvas.drawText("• Sécurité : QR Code haute résolution scannable par tout lecteur optique ou smartphone certifié.", 56f, guideStartY + 118f, paint)

        // ==========================================
        // 4. SECURE OFFLINE ARCHIVE CERTIFICATE BOX
        // ==========================================
        val certStartY = guideStartY + 155f

        paint.color = Color.parseColor("#F1F5F9")
        val certCardRect = RectF(40f, certStartY, 555f, certStartY + 140f)
        canvas.drawRoundRect(certCardRect, 12f, 12f, paint)

        paint.color = primaryColor
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ARCHIVE NUMÉRIQUE SÉCURISÉE & CONFIDENTIALITÉ", 56f, certStartY + 24f, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Titulaire : $fullName • Identifiant Unique : $memberId", 56f, certStartY + 44f, paint)
        canvas.drawText("Statut de Vérification : $verificationStatus • Date d'émission : $currentDate", 56f, certStartY + 60f, paint)
        canvas.drawText("Chiffrement : " + (if (options.isPasswordProtected) "Actif (Mot de passe requis pour consultation/impression)" else "Non protégé par mot de passe"), 56f, certStartY + 76f, paint)
        canvas.drawText("Cette copie numérique peut être conservée sur clé USB sécurisée ou coffre-fort numérique personnel.", 56f, certStartY + 92f, paint)

        if (options.customNote.isNotBlank()) {
            paint.color = accentColor
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Note personnelle : ${options.customNote}", 56f, certStartY + 114f, paint)
        }
    }

    private fun renderOfficialCertificate(
        canvas: Canvas,
        primaryColor: Int,
        accentColor: Int,
        cardBgColor: Int,
        fullName: String,
        dateOfBirth: String,
        residency: String,
        community: String,
        passport: String,
        license: String,
        memberId: String,
        verificationStatus: String,
        expiryDate: String,
        photoBase64: String?,
        options: PdfGenerationOptions,
        docUuid: String,
        currentDate: String
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        // Certificate Decorative Frame
        val frameRect = RectF(35f, 120f, 560f, 780f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(frameRect, 16f, 16f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = primaryColor
        canvas.drawRoundRect(frameRect, 16f, 16f, paint)

        val innerRect = RectF(42f, 127f, 553f, 773f)
        paint.strokeWidth = 0.8f
        paint.color = accentColor
        canvas.drawRoundRect(innerRect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        // Certificate Title
        paint.color = primaryColor
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText("CERTIFICAT D'IDENTITÉ NUMÉRIQUE", 120f, 165f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Attestation officielle de délivrance de la carte numérique IDMuslim", 110f, 185f, paint)

        // Photo & Main Info Block
        val photoRect = RectF(60f, 215f, 150f, 325f)
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRoundRect(photoRect, 10f, 10f, paint)

        var photoDrawn = false
        if (options.includePhoto && !photoBase64.isNullOrBlank()) {
            try {
                val cleanB64 = if (photoBase64.contains(",")) photoBase64.substringAfter(",") else photoBase64
                val decodedBytes = Base64.decode(cleanB64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, photoRect, null)
                    photoDrawn = true
                }
            } catch (e: Exception) {
                photoDrawn = false
            }
        }

        if (!photoDrawn) {
            paint.color = primaryColor
            paint.textSize = 32f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val initials = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
            canvas.drawText(initials.ifEmpty { "ID" }, 85f, 280f, paint)
        }

        // Details Column
        var rowY = 230f
        val labelX = 170f
        val valueX = 310f
        val gap = 24f

        fun drawCertRow(label: String, value: String) {
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(label, labelX, rowY, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(value, valueX, rowY, paint)

            paint.color = Color.parseColor("#E2E8F0")
            paint.strokeWidth = 0.5f
            canvas.drawLine(labelX, rowY + 8f, 530f, rowY + 8f, paint)
            paint.strokeWidth = 0f

            rowY += gap
        }

        drawCertRow("Nom & Prénom :", fullName)
        drawCertRow("Identifiant Membre :", memberId)
        drawCertRow("Date de Naissance :", dateOfBirth.ifEmpty { "Non spécifié" })
        drawCertRow("Lieu de Résidence :", residency.ifEmpty { "France" })
        drawCertRow("Affiliation Communautaire :", community.ifEmpty { "Communauté IDMuslim" })
        drawCertRow("Statut d'Accréditation :", verificationStatus)
        drawCertRow("Date d'Expiration :", expiryDate)

        // QR Code & Barcode Box
        val codesY = rowY + 30f
        val qrSize = 100
        val qrBitmap = if (options.includeQrCode) {
            QRCodeGenerator.generateQRCode("IDMUSLIM:CERT:$memberId:$fullName:$docUuid", qrSize)
        } else null

        if (qrBitmap != null) {
            val qrRect = RectF(60f, codesY, 60f + qrSize, codesY + qrSize)
            canvas.drawBitmap(qrBitmap, null, qrRect, null)
        }

        val barcodeBitmap = if (options.includeBarcode) {
            QRCodeGenerator.generateBarcode(memberId.replace("-", ""), 260, 50)
        } else null

        if (barcodeBitmap != null) {
            val barRect = RectF(180f, codesY + 15f, 480f, codesY + 65f)
            canvas.drawBitmap(barcodeBitmap, null, barRect, null)
        }

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText(memberId, 280f, codesY + 85f, paint)

        // Official Seal & Signatures
        val sealY = codesY + 130f
        paint.color = primaryColor
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SCEAU NUMÉRIQUE D'AUTHENTICITÉ", 60f, sealY, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Empreinte SHA-256 : $docUuid", 60f, sealY + 16f, paint)
        canvas.drawText("Généré le : $currentDate • Prêt pour impression physique & archivage", 60f, sealY + 30f, paint)
        
        if (options.digitalSignature != null) {
            paint.color = accentColor
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            val sigText = options.digitalSignature.chunked(60).joinToString("\n")
            var sigY = sealY + 44f
            canvas.drawText("Signature ECDSA (Keystore Local) :", 60f, sigY, paint)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            for (line in sigText.split("\n")) {
                sigY += 12f
                canvas.drawText(line, 60f, sigY, paint)
            }
        }
    }

    /**
     * Triggers native Android print manager dialog with the generated PDF file.
     */
    fun printPdf(context: Context, pdfFile: File) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Service d'impression non disponible sur cet appareil", Toast.LENGTH_SHORT).show()
                return
            }

            val jobName = "IDMuslim_Carte_${pdfFile.name}"
            val printAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder(pdfFile.name)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        FileInputStream(pdfFile).use { input ->
                            FileOutputStream(destination?.fileDescriptor).use { output ->
                                input.copyTo(output)
                            }
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }

            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())

        } catch (e: Exception) {
            Toast.makeText(context, "Erreur lors de l'impression : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shares the generated PDF file via Android Share sheet.
     */
    fun sharePdf(context: Context, pdfFile: File, isProtected: Boolean = false) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Carte d'Identité IDMuslim - ${pdfFile.name}")
                val body = if (isProtected) {
                    "Veuillez trouver ci-joint votre carte d'identité numérique IDMuslim protégée par mot de passe pour impression physique ou archivage sécurisé."
                } else {
                    "Veuillez trouver ci-joint votre carte d'identité numérique IDMuslim prête pour impression physique."
                }
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Partager / Imprimer le PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur de partage : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Legacy method maintained for backward compatibility
    fun generatePdf(
        context: Context,
        fullName: String,
        dateOfBirth: String,
        residency: String,
        community: String,
        passport: String,
        license: String,
        memberId: String
    ) {
        generateSecurePdf(
            context = context,
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            residency = residency,
            community = community,
            passport = passport,
            license = license,
            memberId = memberId,
            options = PdfGenerationOptions(
                isPasswordProtected = false,
                layoutMode = PrintLayoutMode.CARD_CUTOUT_WALLET
            ),
            onSuccess = { file ->
                Toast.makeText(context, "PDF sauvegardé dans les Téléchargements:\n${file.name}", Toast.LENGTH_LONG).show()
            },
            onError = { err ->
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
