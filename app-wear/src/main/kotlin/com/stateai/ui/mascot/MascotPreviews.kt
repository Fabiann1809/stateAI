package com.stateai.ui.mascot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.stateai.ui.theme.StateAiColors

private class ExpressionProvider : PreviewParameterProvider<MascotExpression> {
    override val values = MascotExpression.entries.asSequence()
}

@Preview(device = WEAR_LARGE_ROUND, showSystemUi = true)
@Composable
private fun MascotPreview(@PreviewParameter(ExpressionProvider::class) expression: MascotExpression) {
    Box(Modifier.fillMaxSize().background(StateAiColors.Background), contentAlignment = Alignment.Center) {
        Mascot(expression)
    }
}

/** Large round Wear OS device of Android Studio previews. */
private const val WEAR_LARGE_ROUND = "id:wearos_large_round"
