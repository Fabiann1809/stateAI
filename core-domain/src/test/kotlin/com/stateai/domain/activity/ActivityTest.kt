package com.stateai.domain.activity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class ActivityTest {
    @Test
    fun `activities with same category and normalized name share a key`() {
        val first = Activity(ActivityId("1"), ActivityCategory.STUDY, ActivityName.of("BD"))
        val second = Activity(ActivityId("2"), ActivityCategory.STUDY, ActivityName.of("bd "))

        assertEquals(first.key, second.key)
    }

    @Test
    fun `same name in different categories is a different activity`() {
        val study = Activity(ActivityId("1"), ActivityCategory.STUDY, ActivityName.of("bd"))
        val work = Activity(ActivityId("2"), ActivityCategory.DEEP_WORK, ActivityName.of("bd"))

        assertNotEquals(study.key, work.key)
    }

    @Test
    fun `new activities are active`() {
        val activity = Activity(ActivityId("1"), ActivityCategory.OTHER, name = null)

        assertEquals(ActivityStatus.ACTIVE, activity.status)
    }
}
