package com.example.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileApduService : HostApduService() {

    companion object {
        private const val TAG = "ProfileApduService"
        // AID mapping corresponds to F0010203040506
        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
            0x07.toByte(), 0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
            0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )
        // Global static to hold payload
        var activePayload = "{}"
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return byteArrayOf(0x6F.toByte(), 0x00.toByte())
        }

        if (commandApdu.contentEquals(SELECT_APDU)) {
            Log.d(TAG, "Application selected via NFC")
            
            // Log the activity
            val dao = com.example.data.AppDatabase.getDatabase(this).activityLogDao()
            CoroutineScope(Dispatchers.IO).launch {
                dao.insertLog(
                    com.example.data.ActivityLogEntity(
                        timestamp = System.currentTimeMillis(),
                        actionType = "NFC_VERIFIED",
                        description = "Digital ID read via NFC terminal"
                    )
                )
            }
            
            // Acknowledge select
            return byteArrayOf(0x90.toByte(), 0x00.toByte())
        }

        // Return current active payload (e.g. member ID JSON)
        val response = activePayload.toByteArray(Charsets.UTF_8)
        // Add success trailer
        val finalResponse = ByteArray(response.size + 2)
        System.arraycopy(response, 0, finalResponse, 0, response.size)
        finalResponse[response.size] = 0x90.toByte()
        finalResponse[response.size + 1] = 0x00.toByte()
        return finalResponse
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }
}
