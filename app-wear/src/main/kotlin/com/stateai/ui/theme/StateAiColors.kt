package com.stateai.ui.theme

import androidx.compose.ui.graphics.Color

/** Color tokens of the stateAI design system (dark only, OLED). */
object StateAiColors {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF14161B)
    val Surface2 = Color(0xFF1E2128)
    val Outline = Color(0xFF2C303A)

    val Text1 = Color(0xFFFFFFFF)
    val Text2 = Color(0xB3FFFFFF)
    val Text3 = Color(0x73FFFFFF)

    val Focused = Color(0xFF34D399)
    val Normal = Color(0xFF8EA2FF)
    val Overloaded = Color(0xFFFFB067)
    val Exhausted = Color(0xFFFF6B8A)
    val NoData = Color(0xFF6B7280)

    val Accent = Color(0xFF5EEAD4)

    /** Background glow of the main screen: a very dark turquoise that fades into black. */
    val GlowCenter = Color(0xFF0D2B2A)
    val GlowEdge = Color(0xFF050E0E)
    val OnAccent = Color(0xFF04201C)

    /** Everything in ambient mode: no saturated color. */
    val Ambient = Color(0xFFB8BCC6)
    val AmbientIndicator = Color(0xFF9CA3AF)
}
