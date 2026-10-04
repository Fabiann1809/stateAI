package com.stateai.domain.energy

/**
 * Word shown next to the energy percentage. "Low" follows [EnergyBudget.isLow], so the label and the
 * low-energy suggestion always agree (decision 27).
 */
enum class EnergyBand {
    HIGH,
    MEDIUM,
    LOW,
    ;

    companion object {
        const val HIGH_FROM = 60.0

        fun of(budget: EnergyBudget): EnergyBand = when {
            budget.isLow -> LOW
            budget.level >= HIGH_FROM -> HIGH
            else -> MEDIUM
        }
    }
}
