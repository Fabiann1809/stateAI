package com.stateai.ui.components

import com.stateai.domain.state.DisplayState

/** A point of the day chart: position along the x range (0-1) and its state. */
data class ChartPoint(val x: Float, val state: DisplayState)

/** One bar: [value] from 0 to 1, or null for "no data" (drawn as a short dashed outline). */
data class Bar(val value: Float?, val highlighted: Boolean)
