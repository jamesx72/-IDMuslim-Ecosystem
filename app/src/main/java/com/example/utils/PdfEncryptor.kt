package com.example.utils

import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.UUID

/**
 * Standard PDF 1.4-1.7 Encryption Implementation (Revision 3, 128-bit Standard Security Handler)
 * Enables generating password-protected PDFs readable by Adobe Acrobat, Chrome, Android PDF Viewer, etc.
 */
object PdfEncryptor {

    private val PADDING = byteArrayOf(
        0x28.toByte(), 0xBF.toByte(), 0x4E.toByte(), 0x5E.toByte(), 0x4E.toByte(), 0x75.toByte(), 0x8A.toByte(), 0x41.toByte(),
        0x64.toByte(), 0x00.toByte(), 0x4E.toByte(), 0x56.toByte(), 0xFF.toByte(), 0xFA.toByte(), 0x01.toByte(), 0x08.toByte(),
        0x2E.toByte(), 0x2E.toByte(), 0x00.toByte(), 0xB6.toByte(), 0xD0.toByte(), 0x68.toByte(), 0x3E.toByte(), 0x80.toByte(),
        0x2F.toByte(), 0x0C.toByte(), 0xA9.toByte(), 0xFE.toByte(), 0x64.toByte(), 0x53.toByte(), 0x69.toByte(), 0x7A.toByte()
    )

    private const val PERMISSIONS = -3904 // Allow print, copy, etc.

    class Rc4(key: ByteArray) {
        private val s = IntArray(256) { it }

        init {
            var j = 0
            for (i in 0 until 256) {
                j = (j + s[i] + (key[i % key.size].toInt() and 0xFF)) and 0xFF
                val temp = s[i]
                s[i] = s[j]
                s[j] = temp
            }
        }

        fun crypt(data: ByteArray): ByteArray {
            val out = ByteArray(data.size)
            val state = s.clone()
            var i = 0
            var j = 0
            for (k in data.indices) {
                i = (i + 1) and 0xFF
                j = (j + state[i]) and 0xFF
                val temp = state[i]
                state[i] = state[j]
                state[j] = temp
                val t = (state[i] + state[j]) and 0xFF
                out[k] = (data[k].toInt() xor state[t]).toByte()
            }
            return out
        }
    }

    private fun md5(vararg chunks: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("MD5")
        for (chunk in chunks) {
            md.update(chunk)
        }
        return md.digest()
    }

    private fun padPassword(password: String): ByteArray {
        val bytes = password.toByteArray(Charsets.ISO_8859_1)
        val result = ByteArray(32)
        if (bytes.size >= 32) {
            System.arraycopy(bytes, 0, result, 0, 32)
        } else {
            System.arraycopy(bytes, 0, result, 0, bytes.size)
            System.arraycopy(PADDING, 0, result, bytes.size, 32 - bytes.size)
        }
        return result
    }

    private fun intTo4BytesLittleEndian(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun computeOwnerValue(userPadded: ByteArray, ownerPadded: ByteArray): ByteArray {
        var digest = md5(ownerPadded)
        for (i in 0 until 50) {
            digest = md5(digest.copyOf(16))
        }
        val ownerKey = digest.copyOf(16)
        var result = Rc4(ownerKey).crypt(userPadded)
        for (i in 1..19) {
            val iterKey = ByteArray(16) { k -> (ownerKey[k].toInt() xor i).toByte() }
            result = Rc4(iterKey).crypt(result)
        }
        return result
    }

    private fun computeMasterKey(userPadded: ByteArray, oValue: ByteArray, pValue: Int, fileId: ByteArray): ByteArray {
        val pBytes = intTo4BytesLittleEndian(pValue)
        var digest = md5(userPadded, oValue, pBytes, fileId)
        for (i in 0 until 50) {
            digest = md5(digest.copyOf(16))
        }
        return digest.copyOf(16)
    }

    private fun computeUserValue(masterKey: ByteArray, fileId: ByteArray): ByteArray {
        val digest = md5(PADDING, fileId)
        var result = Rc4(masterKey).crypt(digest)
        for (i in 1..19) {
            val iterKey = ByteArray(16) { k -> (masterKey[k].toInt() xor i).toByte() }
            result = Rc4(iterKey).crypt(result)
        }
        val uValue = ByteArray(32)
        System.arraycopy(result, 0, uValue, 0, 16)
        System.arraycopy(ByteArray(16), 0, uValue, 16, 16)
        return uValue
    }

    private fun toHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Applies standard PDF password protection to raw PDF byte array.
     */
    fun encryptPdf(pdfBytes: ByteArray, userPassword: String): ByteArray {
        if (userPassword.isEmpty()) return pdfBytes

        try {
            val content = String(pdfBytes, Charsets.ISO_8859_1)
            val userPadded = padPassword(userPassword)
            val ownerPadded = padPassword(userPassword) // Same for user & owner

            // Generate deterministic 16-byte File ID
            val fileId = md5(UUID.randomUUID().toString().toByteArray())

            val oValue = computeOwnerValue(userPadded, ownerPadded)
            val masterKey = computeMasterKey(userPadded, oValue, PERMISSIONS, fileId)
            val uValue = computeUserValue(masterKey, fileId)

            // Parse objects in PDF
            // Regex to find objects: (\d+)\s+(\d+)\s+obj([\s\S]*?)endobj
            val objRegex = Regex("(\\d+)\\s+(\\d+)\\s+obj([\\s\\S]*?)endobj")
            val matches = objRegex.findAll(content).toList()

            if (matches.isEmpty()) {
                return pdfBytes
            }

            var maxObjNum = 0
            val objMap = mutableListOf<Triple<Int, Int, String>>() // objNum, genNum, body

            for (match in matches) {
                val objNum = match.groupValues[1].toInt()
                val genNum = match.groupValues[2].toInt()
                var body = match.groupValues[3]
                if (objNum > maxObjNum) maxObjNum = objNum

                // Encrypt stream if present
                val streamStartTag = "stream\r\n"
                val streamStartAlt = "stream\n"
                val streamEndTag = "endstream"

                var streamStartIdx = body.indexOf(streamStartTag)
                var streamHeaderLen = streamStartTag.length
                if (streamStartIdx == -1) {
                    streamStartIdx = body.indexOf(streamStartAlt)
                    streamHeaderLen = streamStartAlt.length
                }

                val streamEndIdx = body.lastIndexOf(streamEndTag)

                if (streamStartIdx != -1 && streamEndIdx > streamStartIdx) {
                    val preStream = body.substring(0, streamStartIdx + streamHeaderLen)
                    val rawStream = body.substring(streamStartIdx + streamHeaderLen, streamEndIdx)
                    val postStream = body.substring(streamEndIdx)

                    // Object Key: MD5(masterKey + objNum(3 LE) + genNum(2 LE))
                    val objNumBytes = byteArrayOf(
                        (objNum and 0xFF).toByte(),
                        ((objNum shr 8) and 0xFF).toByte(),
                        ((objNum shr 16) and 0xFF).toByte()
                    )
                    val genNumBytes = byteArrayOf(
                        (genNum and 0xFF).toByte(),
                        ((genNum shr 8) and 0xFF).toByte()
                    )
                    val objKey = md5(masterKey, objNumBytes, genNumBytes).copyOf(16)

                    val streamBytes = rawStream.toByteArray(Charsets.ISO_8859_1)
                    val encryptedStream = Rc4(objKey).crypt(streamBytes)
                    val encryptedStreamStr = String(encryptedStream, Charsets.ISO_8859_1)

                    body = preStream + encryptedStreamStr + postStream
                }

                objMap.add(Triple(objNum, genNum, body))
            }

            val encryptObjNum = maxObjNum + 1
            val oHex = toHex(oValue)
            val uHex = toHex(uValue)
            val idHex = toHex(fileId)

            val encryptObjBody = """
<<
  /Filter /Standard
  /V 2
  /R 3
  /Length 128
  /P $PERMISSIONS
  /O <$oHex>
  /U <$uHex>
>>"""
            objMap.add(Triple(encryptObjNum, 0, encryptObjBody))

            // Reconstruct PDF with clean XREF table and Trailer
            val outStream = ByteArrayOutputStream()
            val header = "%PDF-1.4\r\n%\u00e2\u00e3\u00cf\u00d3\r\n"
            outStream.write(header.toByteArray(Charsets.ISO_8859_1))

            val xrefOffsets = mutableMapOf<Int, Long>()

            for (item in objMap) {
                val offset = outStream.size().toLong()
                xrefOffsets[item.first] = offset
                val objHeader = "${item.first} ${item.second} obj"
                val objFooter = "\r\nendobj\r\n"
                outStream.write(objHeader.toByteArray(Charsets.ISO_8859_1))
                outStream.write(item.third.toByteArray(Charsets.ISO_8859_1))
                outStream.write(objFooter.toByteArray(Charsets.ISO_8859_1))
            }

            // Write XREF
            val startXref = outStream.size().toLong()
            val totalObjs = encryptObjNum + 1
            val xrefHeader = "xref\r\n0 $totalObjs\r\n0000000000 65535 f \r\n"
            outStream.write(xrefHeader.toByteArray(Charsets.ISO_8859_1))

            for (i in 1..encryptObjNum) {
                val offset = xrefOffsets[i] ?: 0L
                val entry = String.format(java.util.Locale.US, "%010d 00000 n \r\n", offset)
                outStream.write(entry.toByteArray(Charsets.ISO_8859_1))
            }

            // Extract Root / Catalog ID from original trailer or default to 1 0 R
            var rootObj = "1 0 R"
            val rootMatch = Regex("/Root\\s+(\\d+\\s+\\d+\\s+R)").find(content)
            if (rootMatch != null) {
                rootObj = rootMatch.groupValues[1]
            }

            val trailer = """
trailer
<<
  /Size $totalObjs
  /Root $rootObj
  /Encrypt $encryptObjNum 0 R
  /ID [ <$idHex> <$idHex> ]
>>
startxref
$startXref
%%EOF
""".trimIndent()
            outStream.write(trailer.toByteArray(Charsets.ISO_8859_1))

            return outStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return pdfBytes
        }
    }
}
