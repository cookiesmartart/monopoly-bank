package com.github.cookiesmartart.monopolybank.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator

private const val TONE_DURATION_MS = 120
private const val VIBRATION_DURATION_MS = 60L
private const val TONE_RELEASE_DELAY_MS = 300L

/** Plays a short confirmation beep and/or a short buzz after a successful transaction or NFC scan. */
fun playTransactionFeedback(context: Context, soundEnabled: Boolean, vibrationEnabled: Boolean) {
    if (soundEnabled) playBeep()
    if (vibrationEnabled) vibrate(context)
}

private fun playBeep() {
    val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME)
    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS)
    Handler(Looper.getMainLooper()).postDelayed({ toneGenerator.release() }, TONE_RELEASE_DELAY_MS)
}

@Suppress("DEPRECATION")
private fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (!vibrator.hasVibrator()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(VIBRATION_DURATION_MS)
    }
}
