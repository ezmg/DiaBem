package com.velkyss.diabem.core.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Condition {
    @SerialName("diabetes_type_2") DIABETES_TYPE_2,
    @SerialName("hypertension") HYPERTENSION,
}

/**
 * Optional limits given by the person's own nutritionist. They always win over the defaults.
 * The app never suggests or changes these numbers by itself.
 */
@Serializable
data class CustomLimits(
    val maxCarbsPerMealG: Double? = null,
    val maxRiceSpoonsPerMeal: Int? = null,
) {
    init {
        require(maxCarbsPerMealG == null || maxCarbsPerMealG > 0) { "maxCarbsPerMealG must be positive" }
        require(maxRiceSpoonsPerMeal == null || maxRiceSpoonsPerMeal > 0) { "maxRiceSpoonsPerMeal must be positive" }
    }
}

/** The person's health profile. Stored only on the device. */
@Serializable
data class HealthProfile(
    val conditions: Set<Condition>,
    val customLimits: CustomLimits = CustomLimits(),
) {
    init {
        require(conditions.isNotEmpty()) { "A profile needs at least one condition" }
    }

    val hasDiabetes: Boolean get() = Condition.DIABETES_TYPE_2 in conditions
    val hasHypertension: Boolean get() = Condition.HYPERTENSION in conditions
}
