package com.stateai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stateai.ui.theme.StateAiColors

/** Dots along the lower arc: the current page is a longer turquoise pill, the others small and dim. */
@Composable
fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { page ->
            val selected = page == current
            val length by animateDpAsState(if (selected) SELECTED else DOT, label = "dotLength")
            Box(Modifier.width(length).height(DOT).background(dotColor(selected), CircleShape))
        }
    }
}

/** The same dots stacked on the right edge, for the pages that scroll vertically. */
@Composable
fun VerticalPageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(GAP),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(count) { page ->
            val selected = page == current
            val length by animateDpAsState(if (selected) SELECTED else DOT, label = "dotLength")
            Box(Modifier.size(width = DOT, height = length).background(dotColor(selected), CircleShape))
        }
    }
}

@Composable
private fun dotColor(selected: Boolean): Color {
    val color by animateColorAsState(if (selected) StateAiColors.Accent else DIM, label = "dotColor")
    return color
}

private val DIM = Color.White.copy(alpha = 0.35f)
private val DOT: Dp = 5.dp
private val SELECTED: Dp = 13.dp
private val GAP: Dp = 4.dp
