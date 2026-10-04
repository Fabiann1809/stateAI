package com.stateai.domain.haptics

/** Plays a haptic event on the device. */
fun interface HapticPlayer {
    fun play(event: HapticEvent)
}
