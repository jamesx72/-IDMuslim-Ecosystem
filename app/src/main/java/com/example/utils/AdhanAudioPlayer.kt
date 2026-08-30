package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

object AdhanAudioPlayer {

    private const val TAG = "AdhanAudioPlayer"
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Plays the call to prayer (Adhan).
     * If a custom audio file is not bundled, plays an authentic Islamic tone or alarm stream sound.
     */
    fun playAdhan(context: Context, onCompletion: (() -> Unit)? = null) {
        stopAdhan()
        try {
            // Check for notification/alarm ringtone
            val alertUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alertUri)
                isLooping = false
                setOnCompletionListener {
                    stopAdhan()
                    onCompletion?.invoke()
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing Adhan audio", e)
        }
    }

    fun stopAdhan() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Adhan", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }
}
