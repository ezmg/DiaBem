package com.velkyss.diabem.core.model

import kotlinx.serialization.Serializable

/** Nutrients the app tracks. Units are part of the name. */
@Serializable
enum class Nutrient {
    ENERGY_KCAL,
    CARBOHYDRATE_G,
    FIBER_G,
    PROTEIN_G,
    LIPIDS_G,
    SODIUM_MG,
    POTASSIUM_MG,
}

/**
 * A set of nutrient amounts. A nutrient that is absent from [values] has no data in the
 * source table ("not analysed"): it is unknown, never zero.
 */
@Serializable
data class Nutrients(val values: Map<Nutrient, Double> = emptyMap()) {

    operator fun get(n: Nutrient): Double? = values[n]

    fun isKnown(n: Nutrient): Boolean = n in values

    /** Multiplies every known value by [factor]. */
    fun scaled(factor: Double): Nutrients = Nutrients(values.mapValues { it.value * factor })

    /**
     * Adds two nutrient sets. If a nutrient is unknown in either side, the result is unknown,
     * so a missing value can never hide inside a total.
     */
    operator fun plus(other: Nutrients): Nutrients {
        val keys = values.keys intersect other.values.keys
        return Nutrients(keys.associateWith { values.getValue(it) + other.values.getValue(it) })
    }

    companion object {
        /** Neutral element for [plus]: every nutrient known and equal to zero. */
        val ZERO = Nutrients(Nutrient.entries.associateWith { 0.0 })
    }
}
