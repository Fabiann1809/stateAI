package com.stateai.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.stateai.R

private val WEIGHTS =
    listOf(FontWeight.Light, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold)

/** Lexend, one variable font file: each weight picks its own point on the weight axis. */
val Lexend = FontFamily(
    WEIGHTS.map { weight ->
        Font(
            resId = R.font.lexend,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
        )
    },
)
