package com.stateai.ui.mascot

import androidx.annotation.DrawableRes
import com.stateai.R

/**
 * The mascot's expressions (`docs/mascot.md`). Each one is three stacked layers: body, tip (which
 * can sway on its own) and face. The mascot never reflects energy; it only changes expression.
 */
enum class MascotExpression(
    @param:DrawableRes val body: Int,
    @param:DrawableRes val tip: Int,
    @param:DrawableRes val face: Int,
) {
    REST(R.drawable.mascot_rest_body, R.drawable.mascot_rest_tip, R.drawable.mascot_rest_face),
    LISTENING(R.drawable.mascot_listening_body, R.drawable.mascot_listening_tip, R.drawable.mascot_listening_face),
    THINKING(R.drawable.mascot_thinking_body, R.drawable.mascot_thinking_tip, R.drawable.mascot_thinking_face),
    HAPPY(R.drawable.mascot_happy_body, R.drawable.mascot_happy_tip, R.drawable.mascot_happy_face),
    TIRED(R.drawable.mascot_tired_body, R.drawable.mascot_tired_tip, R.drawable.mascot_tired_face),
}
