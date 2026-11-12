package com.velkyss.diabem.core.health

import com.velkyss.diabem.core.model.MealType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How trustworthy a threshold is. Shown in the docs and usable by the UI. */
@Serializable
enum class CriterionStatus {
    /** Taken directly from an official source. */
    @SerialName("official") OFFICIAL,

    /** Calculated from official values with a documented rule. */
    @SerialName("derived") DERIVED,

    /** No clear official source: provisional value that a nutritionist must review. */
    @SerialName("pending_review") PENDING_REVIEW,
}

/** Green up to [greenMax], amber up to [amberMax], red above. */
@Serializable
data class TrafficThresholds(
    val greenMax: Double,
    val amberMax: Double,
) {
    init {
        require(greenMax >= 0 && amberMax >= greenMax) { "Invalid thresholds: $greenMax / $amberMax" }
    }
}

@Serializable
data class CarbCriterion(
    val perServingG: Map<MealType, TrafficThresholds>,
    val status: CriterionStatus,
    val sources: List<String>,
    val note: String,
)

@Serializable
data class SodiumCriterion(
    /** Daily sodium limit for adults (mg). */
    val dailyLimitMg: Double,
    /** Per 100 g of food: green at or below this value (mg). */
    val per100gGreenMaxMg: Double,
    /** Per 100 g of food: red above or at this value (mg). */
    val per100gRedFromMg: Double,
    /** Per serving: red above this share of [dailyLimitMg] (0..1). */
    val perServingRedShareOfDaily: Double,
    val status: CriterionStatus,
    val sources: List<String>,
    val note: String,
)

@Serializable
data class FiberCriterion(
    val dailyMinG: Double,
    val per1000KcalG: Double,
    /** A serving with at least this much fiber is labelled "source of fiber". */
    val sourcePerServingG: Double,
    /** A serving with at least this much fiber is labelled "high in fiber". */
    val highPerServingG: Double,
    val status: CriterionStatus,
    val sources: List<String>,
    val note: String,
)

@Serializable
data class AddedSugarCriterion(
    /** Daily added/free sugar reference (g). */
    val dailyLimitG: Double,
    val status: CriterionStatus,
    val sources: List<String>,
    val note: String,
)

@Serializable
data class HealthCriteria(
    val carbohydrate: CarbCriterion,
    val sodium: SodiumCriterion,
    val fiber: FiberCriterion,
    val addedSugar: AddedSugarCriterion,
)
