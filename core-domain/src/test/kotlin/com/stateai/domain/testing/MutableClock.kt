package com.stateai.domain.testing

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/** Clock whose time only moves when a test advances it. */
class MutableClock(private var now: Instant = Instant.parse("2026-10-04T09:00:00Z")) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId?): Clock = this

    override fun instant(): Instant = now

    fun advanceBy(duration: Duration) {
        now = now.plus(duration.toJavaDuration())
    }
}
