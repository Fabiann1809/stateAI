package com.stateai.domain.profile

import com.stateai.domain.activity.ActivityId
import com.stateai.domain.learning.ActivityLearning
import kotlin.time.Duration.Companion.minutes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ProfileBlenderTest {
    private val blender = ProfileBlender()
    private val study = DefaultCategoryProfiles.STUDY

    @Test
    fun `own weight is n over n plus K`() {
        assertEquals(0.0, blender.ownWeight(0), 1e-9)
        assertEquals(0.5, blender.ownWeight(5), 1e-9)
        assertEquals(0.8, blender.ownWeight(20), 1e-9)
    }

    @Test
    fun `no learning keeps the category profile`() {
        assertSame(study, blender.blend(study, learning = null))
    }

    @Test
    fun `target block moves from the category towards the learned value`() {
        // Study default is 40 min; the activity's sessions last 60 min on average.
        assertEquals(40.minutes, blender.blend(study, learned(sessions = 0)).targetBlock)
        assertEquals(50.minutes, blender.blend(study, learned(sessions = 5)).targetBlock)
        assertEquals(56.minutes, blender.blend(study, learned(sessions = 20)).targetBlock)
    }

    @Test
    fun `movement limit blends the category limit with the learned movement`() {
        // Study (low movement) limit is 0.25; learned mean 0.2 times margin 1.5 is 0.3.
        val blended = blender.blend(study, learned(sessions = 5))

        assertEquals(0.275, blended.movementLimit!!, 1e-9)
    }

    @Test
    fun `feedback sensitivity is carried over`() {
        assertEquals(-0.04, blender.blend(study, learned(sessions = 1, sensitivity = -0.04)).sensitivityAdjustment)
    }

    private fun learned(sessions: Int, sensitivity: Double = 0.0) = ActivityLearning(
        activityId = ActivityId("a"),
        validSessions = sessions,
        meanDuration = 60.minutes,
        meanMovement = 0.2,
        sensitivityAdjustment = sensitivity,
    )
}
