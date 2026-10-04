package com.stateai.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.Text
import com.stateai.ui.theme.StateAiColors
import com.stateai.ui.theme.StateAiDimens

/** Small dim title at the top of a summary page. */
@Composable
fun PageTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = StateAiDimens.Label, color = StateAiColors.Text3, modifier = modifier)
}

/** Calm note for pages without enough data yet. */
@Composable
fun MissingData(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = StateAiDimens.Body,
        color = StateAiColors.Text2,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
