package com.stateai.domain.energy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EnergyBandTest {
    @Test
    fun `bands follow the level and the low flag`() {
        assertEquals(EnergyBand.HIGH, EnergyBand.of(budget(78.0, isLow = false)))
        assertEquals(EnergyBand.HIGH, EnergyBand.of(budget(60.0, isLow = false)))
        assertEquals(EnergyBand.MEDIUM, EnergyBand.of(budget(59.0, isLow = false)))
        assertEquals(EnergyBand.MEDIUM, EnergyBand.of(budget(26.0, isLow = false)))
        assertEquals(EnergyBand.LOW, EnergyBand.of(budget(14.0, isLow = true)))
    }

    private fun budget(level: Double, isLow: Boolean) = EnergyBudget(level, 100 - level, 0.0, isLow)
}
