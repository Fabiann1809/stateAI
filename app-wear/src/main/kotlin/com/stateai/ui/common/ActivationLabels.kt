package com.stateai.ui.common

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.stateai.R
import com.stateai.domain.state.ActivationLevel

/** Spanish UI names of the activation levels (decision 2 in `docs/decisions.md`). */
@StringRes
fun ActivationLevel.labelRes(): Int = when (this) {
    ActivationLevel.LOW -> R.string.level_low
    ActivationLevel.MEDIUM -> R.string.level_medium
    ActivationLevel.HIGH -> R.string.level_high
}

fun ActivationLevel.color(): Color = when (this) {
    ActivationLevel.LOW -> Color(LOW_COLOR)
    ActivationLevel.MEDIUM -> Color(MEDIUM_COLOR)
    ActivationLevel.HIGH -> Color(HIGH_COLOR)
}

private const val LOW_COLOR = 0xFF4FD1C5
private const val MEDIUM_COLOR = 0xFFF6C744
private const val HIGH_COLOR = 0xFFF2706B
