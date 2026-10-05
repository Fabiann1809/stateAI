package com.stateai.demo

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityRepository
import com.stateai.domain.activity.CreateActivity
import com.stateai.domain.activity.CreateActivityResult
import com.stateai.domain.baseline.BaselineRepository
import com.stateai.domain.baseline.UserBaseline
import com.stateai.domain.learning.SegmentLearner
import com.stateai.domain.segment.Segment
import com.stateai.domain.segment.SegmentRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/** What happened when loading the demo history. */
sealed interface DemoLoadResult {
    data class Loaded(val sessions: Int) : DemoLoadResult

    data object AlreadyLoaded : DemoLoadResult

    data object NoRoomForActivities : DemoLoadResult
}

/** Where the demo history goes: the app's own storage and learners, so it behaves like real use. */
class DemoTargets(
    val createActivity: CreateActivity,
    val activities: ActivityRepository,
    val segments: SegmentRepository,
    val baseline: BaselineRepository,
    val learners: List<SegmentLearner>,
)

/**
 * Debug tool: loads [DemoHistory] into the app. Every segment is stored and passed to the same
 * learners as a finished session, in time order, so suggestions, focus hours, the week and the
 * per-activity learning come from the real code. Loading again only adds the sessions still missing
 * (today's later ones, or a new day), never duplicates.
 */
class DemoLoader(private val targets: DemoTargets, private val clock: Clock) {
    suspend fun load(): DemoLoadResult {
        val activities = demoActivities() ?: return DemoLoadResult.NoRoomForActivities
        val now = clock.instant()
        val stored = storedIds(now)
        val missing = DemoHistory(clock.zone, DEMO_DAYS.toInt()).segments(now, activities)
            .filterNot { it.id.value in stored }
        if (missing.isNotEmpty()) {
            ensureBaseline()
            missing.forEach { store(it) }
        }
        return if (missing.isEmpty()) DemoLoadResult.AlreadyLoaded else DemoLoadResult.Loaded(missing.size)
    }

    /** Demo sessions already stored, so loading again (or the next day) only adds what is missing. */
    private suspend fun storedIds(now: Instant): Set<String> {
        val from = LocalDate.now(clock).minusDays(DEMO_DAYS).atStartOfDay(clock.zone).toInstant()
        return targets.segments.observeBetween(from, now).first()
            .map { it.id.value }
            .filter { it.startsWith(DEMO_PREFIX) }
            .toSet()
    }

    /** Sessions never calibrate during the demo: a resting baseline exists from the start. */
    private suspend fun ensureBaseline() {
        if (targets.baseline.load() != null) return
        targets.baseline.save(UserBaseline(RESTING, MEAN_ABS_DIFF, clock.instant()))
    }

    private suspend fun store(segment: Segment) {
        targets.segments.save(segment)
        targets.learners.forEach { it.learn(segment) }
        targets.activities.markUsed(segment.activity.id, segment.start)
    }

    /** Creates the demo activities, or reuses the ones that already exist with the same name and category. */
    private suspend fun demoActivities(): Map<DemoActivity, Activity>? = DemoRoutine.ALL.associateWith { demo ->
        when (val result = targets.createActivity(demo.category, demo.name)) {
            is CreateActivityResult.Created -> result.activity
            is CreateActivityResult.Existing -> result.activity
            CreateActivityResult.LimitReached -> return null
        }
    }

    private companion object {
        const val DEMO_DAYS = 14L
        const val DEMO_PREFIX = "demo-"
        const val RESTING = 65.0
        const val MEAN_ABS_DIFF = 1.2
    }
}
