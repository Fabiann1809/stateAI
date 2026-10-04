package com.stateai.di

import android.content.Context
import com.stateai.domain.haptics.HapticPlayer
import com.stateai.domain.learning.FocusWindowNotifier
import com.stateai.domain.learning.ObserveFocusProfile
import com.stateai.domain.learning.ObserveSuggestedActivity
import com.stateai.domain.segment.SegmentRepository
import com.stateai.domain.summary.ObserveDaySummary
import com.stateai.export.SummaryExporter
import java.time.Clock

/** What the app learns and reports from stored segments. [localClock] is in the device's time zone. */
class InsightsModule(context: Context, segments: SegmentRepository, localClock: Clock, player: HapticPlayer) {
    val observeDaySummary = ObserveDaySummary(segments, localClock)
    val observeSuggestedActivity = ObserveSuggestedActivity(segments, localClock)
    val focusWindowNotifier = FocusWindowNotifier(ObserveFocusProfile(segments, localClock), player, localClock)
    val summaryExporter = SummaryExporter(context, segments)
}
