package com.example.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PassGenerator {
    fun generatePkPass(
        context: Context,
        fullName: String,
        memberId: String,
        dateOfBirth: String,
        verificationStatus: String
    ): File {
        val passJson = """
            {
              "formatVersion": 1,
              "passTypeIdentifier": "pass.com.aistudio.digitalid",
              "serialNumber": "$memberId",
              "teamIdentifier": "AISTUDIOTEAM",
              "organizationName": "Digital ID",
              "description": "Digital Identity Pass",
              "logoText": "Digital ID",
              "foregroundColor": "rgb(255, 255, 255)",
              "backgroundColor": "rgb(27, 77, 62)",
              "generic": {
                "primaryFields": [
                  {
                    "key": "name",
                    "label": "NAME",
                    "value": "${if (fullName.isNotBlank()) fullName else "User"}"
                  }
                ],
                "secondaryFields": [
                  {
                    "key": "dob",
                    "label": "DATE OF BIRTH",
                    "value": "${if (dateOfBirth.isNotBlank()) dateOfBirth else "--"}"
                  }
                ],
                "auxiliaryFields": [
                  {
                    "key": "status",
                    "label": "STATUS",
                    "value": "$verificationStatus"
                  }
                ],
                "backFields": [
                  {
                    "key": "memberId",
                    "label": "MEMBER ID",
                    "value": "$memberId"
                  }
                ]
              },
              "barcode": {
                "message": "$memberId-$verificationStatus",
                "format": "PKBarcodeFormatQR",
                "messageEncoding": "iso-8859-1"
              }
            }
        """.trimIndent()

        val passDir = File(context.cacheDir, "images").apply { mkdirs() }
        val passFile = File(passDir, "DigitalID_$memberId.pkpass")
        ZipOutputStream(FileOutputStream(passFile)).use { zos ->
            // Add pass.json
            zos.putNextEntry(ZipEntry("pass.json"))
            zos.write(passJson.toByteArray())
            zos.closeEntry()
        }
        return passFile
    }
}
