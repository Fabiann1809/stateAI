package com.stateai.di

import android.content.Context
import com.stateai.domain.energy.ObserveEnergy
import com.stateai.domain.haptics.HapticPlayer
import com.stateai.domain.learning.FocusWindowNotifier
import com.stateai.domain.learning.LearningProgress
import com.stateai.domain.learning.LearningRepository
import com.stateai.domain.learning.ObserveFocusProfile
import com.stateai.domain.learning.ObserveSuggestedActivity
import com.stateai.domain.segment.SegmentRecorder
import com.stateai.domain.segment.SegmentRepository
import com.stateai.domain.summary.ObserveDaySummary
import com.stateai.export.SummaryExporter
import java.time.Clock

/** What the app learns and reports from stored segments. [localClock] is in the device's time zone. */
class InsightsModule(
    context: Context,
    segments: SegmentRepository,
    learning: LearningRepository,
    recorder: SegmentRecorder,
    localClock: Clock,
    player: HapticPlayer,
) {
    private val observeFocusProfile = ObserveFocusProfile(segments, localClock)
    val observeDaySummary = ObserveDaySummary(segments, localClock, LearningProgress(learning))
    val observeEnergy = ObserveEnergy(segments, observeFocusProfile, recorder, localClock)
    val observeSuggestedActivity = ObserveSuggestedActivity(segments, localClock)
    val focusWindowNotifier = FocusWindowNotifier(observeFocusProfile, player, localClock)
    val summaryExporter = SummaryExporter(context, segments)
}
