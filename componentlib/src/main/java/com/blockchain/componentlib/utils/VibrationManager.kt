package com.blockchain.componentlib.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity

object VibrationManager {
    private const val VIBRATION_DURATION = 75L

    fun vibrate(
        context: Context,
        duration: Long = VIBRATION_DURATION,
        intensity: VibrationIntensity = VibrationIntensity.High
    ) {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(AppCompatActivity.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(duration, intensity.strength))
        } else {
            // deprecated in API 26
            vib.vibrate(duration)
        }
    }

    // Vibration strength ranges from 0-255
    enum class VibrationIntensity(val strength: Int) {
        High(200),
        Medium(125),
        Low(75)
    }
}
