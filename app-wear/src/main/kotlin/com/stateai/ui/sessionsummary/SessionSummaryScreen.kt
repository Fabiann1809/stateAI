package com.stateai.ui.sessionsummary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.material3.ScreenScaffold
import com.stateai.di.appContainer
import com.stateai.domain.segment.SegmentId
import com.stateai.domain.summary.SessionReport
import com.stateai.ui.components.VerticalPages

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
    ScreenScaffold(timeText = {}) {
        VerticalPages(count = PAGES) { page ->
            if (page == 0) SessionTimelinePage(report, onDone) else SessionStatesPage(report)
        }
    }
}

private const val PAGES = 2
