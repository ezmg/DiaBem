package com.velkyss.diabem.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A moment of the day when the person eats. Several slots can share the same kind of recipes:
 * the mid-morning and the afternoon snack both use [MealType.SNACK] recipes.
 */
@Serializable
enum class MealSlot(val mealType: MealType) {
    @SerialName("breakfast") BREAKFAST(MealType.BREAKFAST),
    @SerialName("morning_snack") MORNING_SNACK(MealType.SNACK),
    @SerialName("lunch") LUNCH(MealType.LUNCH),
    @SerialName("afternoon_snack") AFTERNOON_SNACK(MealType.SNACK),
    @SerialName("dinner") DINNER(MealType.DINNER),
}
