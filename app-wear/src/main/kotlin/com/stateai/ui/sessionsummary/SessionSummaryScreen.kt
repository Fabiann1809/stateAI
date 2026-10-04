package com.stateai.ui.sessionsummary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.material3.ScreenScaffold
import com.stateai.di.appContainer
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.summary.SessionReport
import com.stateai.ui.components.PageIndicator

@Composable
fun SessionSummaryRoute(segmentId: SegmentId, onDone: () -> Unit) {
    val container = appContainer()
    val viewModel: SessionSummaryViewModel = viewModel(
        factory = viewModelFactory { initializer { SessionSummaryViewModel(segmentId, container.segmentRepository) } },
    )
    val report by viewModel.report.collectAsStateWithLifecycle()
    report?.let { SessionSummaryScreen(it, onDone) }
}

/** Summary of the session that just ended: S1 timeline ring, S2 states and pauses. */
@Composable
fun SessionSummaryScreen(report: SessionReport, onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGES })
    ScreenScaffold(timeText = {}) {
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (page == 0) SessionTimelinePage(report, onDone) else SessionStatesPage(report)
                }
            }
            PageIndicator(
                count = PAGES,
                current = pagerState.currentPage,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
            )
        }
    }
}

private const val PAGES = 2
