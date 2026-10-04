package com.stateai.domain.profile

import com.stateai.domain.activity.ActivityCategory
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class DefaultCategoryProfilesTest {
    @ParameterizedTest
    @EnumSource(ActivityCategory::class)
    fun `every category has a profile for itself`(category: ActivityCategory) {
        assertEquals(category, DefaultCategoryProfiles.of(category).category)
    }

    @ParameterizedTest
    @EnumSource(ActivityCategory::class)
    fun `every profile allows at least one vibration per hour`(category: ActivityCategory) {
        assertTrue(DefaultCategoryProfiles.of(category).maxVibrationsPerHour >= 1)
    }

    @Test
    fun `target blocks are the midpoint of the specified ranges`() {
        assertEquals(75.minutes, DefaultCategoryProfiles.DEEP_WORK.targetBlock)
        assertEquals(40.minutes, DefaultCategoryProfiles.STUDY.targetBlock)
        assertEquals(40.minutes, DefaultCategoryProfiles.READING.targetBlock)
        assertEquals(45.minutes, DefaultCategoryProfiles.COLLAB.targetBlock)
        assertEquals(45.minutes, DefaultCategoryProfiles.OTHER.targetBlock)
    }

    @Test
    fun `reading expects the least movement and collaboration the most`() {
        assertEquals(MovementLevel.VERY_LOW, DefaultCategoryProfiles.READING.normalMovement)
        assertEquals(MovementLevel.MEDIUM_HIGH, DefaultCategoryProfiles.COLLAB.normalMovement)
    }
}
