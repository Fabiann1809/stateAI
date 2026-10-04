package com.stateai.ui.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.stateai.R
import com.stateai.ui.mascot.AnimatedMascot
import com.stateai.ui.mascot.MascotExpression

/** Debug-only gallery of the five mascot expressions, to compare them with the design sheet. */
@Composable
fun MascotDebugScreen() {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.debug_mascot_title)) } }
            items(MascotExpression.entries) { expression ->
                AnimatedMascot(expression, contentDescription = expression.name)
            }
        }
    }
}
