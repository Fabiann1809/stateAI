package com.stateai.domain.baseline

import java.time.Instant

/** Resting reference of the person, shared by all activities. Thresholds are relative to it. */
data class UserBaseline(val restingHeartRate: Double, val heartRateMeanAbsDiff: Double, val updatedAt: Instant)

/** Storage of the personal baseline. */
interface BaselineRepository {
    suspend fun load(): UserBaseline?

    suspend fun save(baseline: UserBaseline)
}
