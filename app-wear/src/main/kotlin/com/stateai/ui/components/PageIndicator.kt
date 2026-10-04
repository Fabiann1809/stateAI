package com.stateai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stateai.ui.theme.StateAiColors

/** Dots on the lower arc: the current page larger and white, the others small and dim. */
@Composable
fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { page ->
            val selected = page == current
            Box(
                Modifier
                    .size(if (selected) SELECTED_DOT else DOT)
                    .background(if (selected) StateAiColors.Text1 else StateAiColors.Text3, CircleShape),
            )
        }
    }
}

private val DOT = 4.dp
private val SELECTED_DOT = 6.dp
