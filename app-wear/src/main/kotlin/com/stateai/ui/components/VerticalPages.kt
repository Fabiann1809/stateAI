package com.stateai.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import com.stateai.ui.theme.StateAiColors

/**
 * Summary pages that scroll vertically, one idea per page: dots on the right edge show where you are
 * and a chevron that bobs on the first page says there is more below.
 */
@Composable
fun VerticalPages(count: Int, modifier: Modifier = Modifier, page: @Composable (Int) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { count })
    Box(modifier.fillMaxSize()) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { page(index) }
        }
        VerticalPageIndicator(
            count = count,
            current = pagerState.currentPage,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = INDICATOR_END),
        )
        if (pagerState.currentPage == 0 && count > 1) {
            MoreBelow(Modifier.align(Alignment.BottomCenter).padding(bottom = CHEVRON_BOTTOM))
        }
    }
}

@Composable
private fun MoreBelow(modifier: Modifier) {
    val bob by rememberInfiniteTransition(label = "chevron").animateFloat(
        initialValue = 0f,
        targetValue = BOB,
        animationSpec = infiniteRepeatable(tween(BOB_MILLIS), RepeatMode.Reverse),
        label = "bob",
    )
    Icon(
        StateAiIcons.Down,
        contentDescription = null,
        tint = StateAiColors.Text2,
        modifier = modifier.graphicsLayer { translationY = bob },
    )
}

private val INDICATOR_END = 10.dp
private val CHEVRON_BOTTOM = 14.dp
private const val BOB = 6f
private const val BOB_MILLIS = 800
