package com.example.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Utility for rich, tactile haptic feedback across key interactions:
 * opening/flipping the ID card, biometric authentication, privacy shield toggles,
 * card unlocking, and verification confirmations.
 */
object HapticHelper {

    private fun getVibrator(context: Context?): Vibrator? {
        if (context == null) return null
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Tactile feedback when the ID card is opened, inspected, or flipped.
     */
    fun performCardFlip(context: Context? = null, haptic: HapticFeedback? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignore
        }

        val vibrator = getVibrator(context) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Distinct multi-pulse feedback for successful authentication / biometric unlock.
     */
    fun performAuthSuccess(context: Context? = null, haptic: HapticFeedback? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignore
        }

        val vibrator = getVibrator(context) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 45, 60, 65)
                val amplitudes = intArrayOf(0, 180, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 45, 60, 65), -1)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Tactile feedback on authentication or validation failure.
     */
    fun performAuthError(context: Context? = null, haptic: HapticFeedback? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignore
        }

        val vibrator = getVibrator(context) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 40, 50, 40, 50)
                val amplitudes = intArrayOf(0, 200, 0, 200, 0, 200)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 50, 40, 50, 40, 50), -1)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Tactile feedback when the ID card is unlocked or presented.
     */
    fun performCardUnlocked(context: Context? = null, haptic: HapticFeedback? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignore
        }

        val vibrator = getVibrator(context) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Crisp click when toggling privacy shield or other interactive toggles.
     */
    fun performPrivacyShieldToggle(context: Context? = null, haptic: HapticFeedback? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (e: Exception) {
            // Ignore
        }

        val vibrator = getVibrator(context) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Subtle click feedback for UI actions, tab switches, button clicks.
     */
    fun performClick(context: Context? = null, haptic: HapticFeedback? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (e: Exception) {
            // Ignore
        }

        val vibrator = getVibrator(context) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(25)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * General success feedback.
     */
    fun performSuccess(context: Context? = null, haptic: HapticFeedback? = null) {
        performAuthSuccess(context, haptic)
    }

    /**
     * QR code scanned successfully or NFC detected.
     */
    fun performScanSuccess(context: Context? = null, haptic: HapticFeedback? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignore
        }

        val vibrator = getVibrator(context) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 30, 40, 40)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 30, 40, 40), -1)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
