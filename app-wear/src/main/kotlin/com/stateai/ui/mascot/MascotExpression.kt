package com.stateai.ui.mascot

/**
 * The mascot's expressions (`docs/mascot.md`): they change the arms, the ears, the eyes and the mouth
 * on its screen face. The mascot never reflects energy; it only changes expression.
 */
enum class MascotExpression {
    /** Calm: waiting on the home screen. */
    REST,

    /** Surprised, with its eyes wide open: the microphone is listening. */
    LISTENING,

    /** Concentrated, with its eyes narrowed: interpreting what was said. */
    THINKING,

    /** Happy: a session is confirmed or a break begins. */
    HAPPY,

    /** Tired: drawn, but not used for now. */
    TIRED,
}
