package com.stateai.ui.mascot

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser

/** How the eyes of an expression are drawn. */
internal sealed interface LumiEyes {
    /** Two vertical pills; they are the ones that blink. */
    data class Pills(val top: Float, val height: Float) : LumiEyes

    /** Two slanted shapes, for concentration. */
    data object Narrowed : LumiEyes

    /** Two closed arcs, for tiredness. */
    data object Closed : LumiEyes
}

/** The mouth: an open stroke or a small ring. */
internal sealed interface LumiMouth {
    data class Line(val path: Path) : LumiMouth

    data object Ring : LumiMouth
}

/** Everything that changes between expressions, in the 256-unit viewport of the design. */
internal class LumiLook(
    val armAngle: Float,
    val earAngle: Float,
    val eyes: LumiEyes,
    val mouth: LumiMouth,
    val cheeks: Boolean,
    val sparks: Boolean,
)

private fun path(data: String): Path = PathParser().parsePathString(data).toPath()

private val LOOKS = mapOf(
    MascotExpression.REST to LumiLook(
        armAngle = 0f,
        earAngle = 0f,
        eyes = LumiEyes.Pills(top = 105f, height = 30f),
        mouth = LumiMouth.Line(path("M118 140Q128 148 138 140")),
        cheeks = false,
        sparks = false,
    ),
    MascotExpression.LISTENING to LumiLook(
        armAngle = 35f,
        earAngle = 0f,
        eyes = LumiEyes.Pills(top = 99f, height = 42f),
        mouth = LumiMouth.Ring,
        cheeks = false,
        sparks = true,
    ),
    MascotExpression.THINKING to LumiLook(
        armAngle = -8f,
        earAngle = 0f,
        eyes = LumiEyes.Narrowed,
        mouth = LumiMouth.Line(path("M118 145H138")),
        cheeks = false,
        sparks = true,
    ),
    MascotExpression.HAPPY to LumiLook(
        armAngle = 22f,
        earAngle = 0f,
        eyes = LumiEyes.Pills(top = 103f, height = 34f),
        mouth = LumiMouth.Line(path("M114 137Q128 154 142 137")),
        cheeks = true,
        sparks = true,
    ),
    MascotExpression.TIRED to LumiLook(
        armAngle = -20f,
        earAngle = 14f,
        eyes = LumiEyes.Closed,
        mouth = LumiMouth.Line(path("M119 146H137")),
        cheeks = false,
        sparks = false,
    ),
)

internal fun lookOf(expression: MascotExpression): LumiLook = checkNotNull(LOOKS[expression])

/** Shapes and colors of the body, the same in every expression. */
internal object LumiShapes {
    val ear: Path = path("M64 92C66 72 78 56 88 57C95 58 100 63 106 69C90 72 76 80 64 92Z")
    val body: Path = path(
        "M128 64C160 64 189 70 198 100C205 125 206 152 196 172C186 192 158 200 128 200" +
            "C98 200 70 192 60 172C50 152 51 125 58 100C67 70 96 64 128 64Z",
    )
    val shade: Path = path(
        "M60 172C70 192 98 200 128 200C158 200 186 192 196 172C180 183 156 187 128 187C100 187 76 183 60 172Z",
    )
    val glint: Path = path("M171 91Q179 92 182 102")
    val narrowedLeft: Path = path("M96 105L110 112V133A7 7 0 0 1 96 133Z")
    val narrowedRight: Path = path("M160 105L146 112V133A7 7 0 0 0 160 133Z")
    val closedLeft: Path = path("M93 123Q103 136 113 123")
    val closedRight: Path = path("M143 123Q153 136 163 123")
    val sparks: List<Path> = listOf(
        path("M198 62L205 54"),
        path("M206 80L217 77"),
        path("M190 46L194 36"),
    )

    val Ice = Color(0xFFAEE2F7)
    val Shade = Color(0xFF7ED3F6)
    val Reflection = Color(0xFFE8F6FF)
    val Screen = Color(0xFF0F1B2D)
    val Face = Color(0xFF00CFF7)
}
