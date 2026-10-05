package com.stateai.demo

import com.stateai.domain.activity.ActivityCategory
import java.time.DayOfWeek

/** One of the demo person's activities; without a name it is the category itself. */
data class DemoActivity(val name: String?, val category: ActivityCategory)

/** A session in the demo routine: what, at which minute of the day, and for how long. */
data class PlannedDemoSession(val activity: DemoActivity, val startMinute: Int, val minutes: Int)

/**
 * The demo person's week: thesis every morning, databases twice a day, and meetings or a novel in the
 * afternoon depending on the day. A steady routine is what lets the app suggest the next activity.
 */
object DemoRoutine {
    val THESIS = DemoActivity("Tesis", ActivityCategory.DEEP_WORK)
    val DATABASES = DemoActivity("Bases de datos", ActivityCategory.STUDY)
    val NOVEL = DemoActivity("Novela", ActivityCategory.READING)
    val MEETINGS = DemoActivity(name = null, ActivityCategory.COLLAB)
    val ALL = listOf(THESIS, DATABASES, NOVEL, MEETINGS)

    private val MEETING_DAYS = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)

    fun sessionsOn(day: DayOfWeek): List<PlannedDemoSession> = listOf(
        PlannedDemoSession(THESIS, startMinute = 9 * 60, minutes = 70),
        PlannedDemoSession(DATABASES, startMinute = 11 * 60 + 30, minutes = 45),
        if (day in MEETING_DAYS) {
            PlannedDemoSession(MEETINGS, startMinute = 15 * 60, minutes = 40)
        } else {
            PlannedDemoSession(NOVEL, startMinute = 15 * 60, minutes = 35)
        },
        PlannedDemoSession(DATABASES, startMinute = 18 * 60, minutes = 40),
    )
}
