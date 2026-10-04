package com.stateai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Horizontal bar split by share, with a small surface gap between parts. Empty parts are skipped. */
@Composable
fun StackedBar(parts: List<Pair<Float, Color>>, modifier: Modifier = Modifier) {
    val visible = parts.filter { it.first > 0f }
    Row(modifier.height(13.dp), horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
        visible.forEach { (weight, color) ->
            Box(Modifier.weight(weight).fillMaxHeight().background(color, RoundedCornerShape(4.dp)))
        }
    }
}
