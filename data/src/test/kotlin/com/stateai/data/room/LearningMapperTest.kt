package com.stateai.data.room

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.learning.ActivityLearning
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LearningMapperTest {
    @Test
    fun `learning survives the round trip through its entity`() {
        val learning = ActivityLearning(ActivityId("a"), 7, 52.minutes, 0.14, sensitivityAdjustment = -0.04)

        assertEquals(learning, learning.toEntity().toDomain())
    }

    @Test
    fun `missing means stay missing`() {
        val learning = ActivityLearning(ActivityId("a"))

        assertEquals(learning, learning.toEntity().toDomain())
    }
}
