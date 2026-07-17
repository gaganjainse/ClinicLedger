package com.clinicledger.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Provides physical haptic and sensory feedback for clinical interactions.
 */
object FeedbackProvider {

    /**
     * Triggers a standard "Confirmation" vibration.
     */
    fun confirm(/** target */ view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = view.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) 
                    as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                view.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (/** ignore */ _: Exception) {
            // Haptics not available
        }
    }

    /**
     * Triggers a "Success/Learned" sensory pulse.
     */
    fun learned(/** target */ view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}
