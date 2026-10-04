package com.stateai.ui.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.stateai.R
import com.stateai.domain.state.DisplayState

/** Spanish UI names of the display states (decisions 2 and 26 in `docs/decisions.md`). */
@StringRes
fun DisplayState.labelRes(): Int = when (this) {
    DisplayState.FOCUSED -> R.string.state_focused
    DisplayState.NORMAL -> R.string.state_normal
    DisplayState.OVERLOADED -> R.string.state_overloaded
    DisplayState.EXHAUSTED -> R.string.state_exhausted
}

fun DisplayState.color(): Color = when (this) {
    DisplayState.FOCUSED -> StateAiColors.Focused
    DisplayState.NORMAL -> StateAiColors.Normal
    DisplayState.OVERLOADED -> StateAiColors.Overloaded
    DisplayState.EXHAUSTED -> StateAiColors.Exhausted
}
