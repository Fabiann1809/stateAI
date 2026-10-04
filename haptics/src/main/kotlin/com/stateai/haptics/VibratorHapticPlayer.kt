package com.stateai.haptics

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import com.stateai.domain.haptics.HapticEvent
import com.stateai.domain.haptics.HapticPlayer

/** Plays haptic events with the device vibrator and logs them (the emulator cannot vibrate). */
class VibratorHapticPlayer(context: Context) : HapticPlayer {
    private val vibrator = context.getSystemService(VibratorManager::class.java).defaultVibrator

    override fun play(event: HapticEvent) {
        val waveform = HapticPatterns.waveformFor(event)
        Log.i(LOG_TAG, "play $event (${waveform.pulseCount} pulses, ${waveform.totalMillis} ms)")
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createWaveform(waveform.timings, waveform.amplitudes, NO_REPEAT))
    }

    companion object {
        const val LOG_TAG = "Haptics"
        private const val NO_REPEAT = -1
    }
}
