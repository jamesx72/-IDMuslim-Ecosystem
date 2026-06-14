package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

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
        var pdfDocument: PdfDocument? = null
        try {
            pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)

            val canvas: Canvas = page.canvas
            val paint = Paint()
            paint.isAntiAlias = true

            // Background
            paint.color = Color.parseColor("#F5F7FA")
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            // Header Banner
            val headerColor = Color.parseColor("#0F5A47") // Emerald Green
            paint.color = headerColor
            canvas.drawRect(0f, 0f, 595f, 150f, paint)

            paint.color = Color.WHITE
            paint.textSize = 36f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("IDMuslim", 50f, 80f, paint)

            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Carte d'Identité Numérique Officielle", 50f, 110f, paint)

            // Card Container
            paint.color = Color.WHITE
            paint.setShadowLayer(10f, 0f, 5f, Color.LTGRAY)
            val cardRect = RectF(50f, 200f, 545f, 650f)
            canvas.drawRoundRect(cardRect, 20f, 20f, paint)
            paint.clearShadowLayer()

            // Card Header
            paint.color = headerColor
            val cardHeaderRect = RectF(50f, 200f, 545f, 250f)
            // Draw a rounded rect for the top part only: 
            // We can draw a full round rect then draw a square rect on the bottom half, or use clip.
            canvas.save()
            canvas.clipRect(cardHeaderRect)
            canvas.drawRoundRect(cardRect, 20f, 20f, paint)
            canvas.restore()
            
            paint.color = Color.WHITE
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("INFORMATIONS PERSONNELLES", 70f, 235f, paint)

            // Draw details inside card
            paint.color = Color.DKGRAY
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            
            var yStart = 300f
            val labelX = 70f
            val valueX = 250f
            val lineSpacing = 40f

            fun drawRow(label: String, value: String) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.GRAY
                canvas.drawText(label, labelX, yStart, paint)
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.BLACK
                canvas.drawText(value, valueX, yStart, paint)
                
                // Add a subtle line
                paint.color = Color.parseColor("#E0E0E0")
                paint.strokeWidth = 1f
                canvas.drawLine(labelX, yStart + 10f, 525f, yStart + 10f, paint)
                paint.strokeWidth = 0f
                
                yStart += lineSpacing
            }

            drawRow("Nom Complet", fullName)
            drawRow("Date de Naissance", dateOfBirth)
            drawRow("Résidence", residency)
            drawRow("Communauté", community)
            drawRow("Passport", passport.ifEmpty { "Non spécifié" })
            drawRow("Permis", license.ifEmpty { "Non spécifié" })
            drawRow("ID Membre", memberId)
            
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            drawRow("Généré le", dateFormat.format(Date()))

            // Footer
            paint.color = Color.GRAY
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Ce document est une preuve numérique sécurisée générée par IDMuslim.", 50f, 750f, paint)
            canvas.drawText("Toute falsification est strictement interdite.", 50f, 770f, paint)

            pdfDocument.finishPage(page)

            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            // Ensure download dir exists
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val file = File(downloadDir, "IDMuslim_Carte_${System.currentTimeMillis()}.pdf")
            
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()

            // Try to open it or notify
            Toast.makeText(context, "PDF sauvegardé dans les Téléchargements:\n${file.name}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur lors de la génération du PDF", Toast.LENGTH_SHORT).show()
        } finally {
            try {
                pdfDocument?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
