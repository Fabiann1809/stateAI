package com.stateai.domain.export

import com.stateai.domain.score.SegmentScorer
import com.stateai.domain.segment.Segment
import java.util.Locale

/**
 * Serializes segment summaries as CSV for offline analysis in `ml-python`. Times are ISO-8601 UTC,
 * durations in seconds and scores from 0 to 100. Free text is quoted and inner quotes are doubled.
 */
class SegmentCsv(private val scorer: SegmentScorer = SegmentScorer()) {
    fun write(segments: List<Segment>): String = buildString {
        appendLine(HEADER.joinToString(SEPARATOR))
        segments.sortedBy { it.start }.forEach { appendLine(row(it).joinToString(SEPARATOR)) }
    }

    private fun row(segment: Segment): List<String> {
        val score = scorer.score(segment)
        return listOf(
            segment.id.value,
            segment.activity.category.name,
            quote(segment.activity.name?.display.orEmpty()),
            segment.start.toString(),
            segment.end.toString(),
            segment.planned.inWholeSeconds.toString(),
            segment.levelTime.low.inWholeSeconds.toString(),
            segment.levelTime.medium.inWholeSeconds.toString(),
            segment.levelTime.high.inWholeSeconds.toString(),
            segment.levelTime.unknown.inWholeSeconds.toString(),
            segment.restlessTime.inWholeSeconds.toString(),
            segment.pauseSuggestions.toString(),
            segment.pauses.size.toString(),
            segment.pauses.count { it.improved }.toString(),
            segment.feedback?.name.orEmpty(),
            format(score.total()),
            format(score.focus),
            format(score.recovery),
            format(score.sustainableLoad),
            format(score.consistency),
        )
    }

    private fun quote(text: String) = "\"" + text.replace("\"", "\"\"") + "\""

    private fun format(value: Double) = String.format(Locale.ROOT, "%.1f", value)

    companion object {
        const val SEPARATOR = ","
        val HEADER = listOf(
            "segment_id", "category", "activity_name", "start", "end", "planned_s",
            "low_s", "medium_s", "high_s", "unknown_s", "restless_s",
            "pause_suggestions", "pauses", "pauses_improved", "feedback",
            "score_total", "score_focus", "score_recovery", "score_load", "score_consistency",
        )
    }
}
