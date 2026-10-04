package com.stateai.ui.common

import java.time.Clock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Emits the current time immediately and then every [period]. */
fun clockTicks(clock: Clock, period: Duration = 1.seconds): Flow<Instant> = flow {
    while (true) {
        emit(clock.instant())
        delay(period)
    }
}
