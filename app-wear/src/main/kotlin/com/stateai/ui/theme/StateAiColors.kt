package com.stateai.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Color tokens of the "Aurora" design system: a deep violet night with a turquoise accent. */
object StateAiColors {
    val Background = Color(0xFF040310)

    /** Cards, buttons and the empty part of the silhouette: white over the aurora at a low alpha. */
    val Surface = Color(0x12FFFFFF)
    val Surface2 = Color(0x14FFFFFF)
    val Outline = Color(0x29FFFFFF)

    val Text1 = Color(0xFFFFFFFF)
    val Text2 = Color(0xFFECE9FC)
    val Text3 = Color(0xFFC4BEEA)

    val Focused = Color(0xFF4BE3B4)
    val Normal = Color(0xFFA3A8FF)
    val Overloaded = Color(0xFFF5B556)
    val Exhausted = Color(0xFFF2688E)
    val NoData = Color(0xFF7F76CC)

    val Accent = Color(0xFF45DAF7)
    val Accent2 = Color(0xFFCDB2FF)
    val Ice = Color(0xFFAEE2F7)
    val AccentSoft = Color(0x2645DAF7)
    val AccentGlow = Color(0x73789FFF)

    /** Dark ink for the text on the gradient buttons. */
    val OnAccent = Color(0xFF0B0724)

    /** Muted violet: tracks, empty marks and the minutes without a state. */
    val Muted = Color(0xFF423A78)

    /** Background: the aurora is a radial gradient plus two soft blobs (turquoise and violet). */
    val AuroraCenter = Color(0xFF2C2170)
    val AuroraMiddle = Color(0xFF140E3A)
    val AuroraEdge = Color(0xFF040310)
    val BlobCyan = Color(0x5245DAF7)
    val BlobViolet = Color(0x66B884FF)

    /** Everything in ambient mode: no saturated color. */
    val Ambient = Color(0xFFB8BCC6)
    val AmbientIndicator = Color(0xFF9CA3AF)

    /** Turquoise to violet, the signature of the primary actions. */
    val AccentGradient: Brush = Brush.linearGradient(listOf(Accent, Color(0xFFB884FF)))
}
