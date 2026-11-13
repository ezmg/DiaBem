package com.velkyss.diabem.core.health

import com.velkyss.diabem.core.model.CompiledRecipe
import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.profile.HealthProfile

enum class TrafficLight { GREEN, AMBER, RED }

enum class FiberLabel { NONE, SOURCE, HIGH }

/** What the app shows for one recipe and one profile. */
data class HealthAssessment(
    val carbs: TrafficLight,
    val sodium: TrafficLight,
    val fiber: FiberLabel,
    /** True when the carb light uses the person's own limit instead of the default thresholds. */
    val usesCustomCarbLimit: Boolean,
    /** Lights that matter for this profile (diabetes -> carbs, hypertension -> sodium). */
    val relevantRed: Boolean,
)

/** Applies [HealthCriteria] to compiled recipes. Pure and deterministic. */
class HealthEvaluator(private val criteria: HealthCriteria) {

    fun carbLight(recipe: CompiledRecipe, meal: MealType, profile: HealthProfile? = null): TrafficLight {
        val carbs = recipe.perServing[Nutrient.CARBOHYDRATE_G] ?: return TrafficLight.RED
        val custom = profile?.customLimits?.maxCarbsPerMealG
        if (custom != null) return if (carbs <= custom) TrafficLight.GREEN else TrafficLight.RED
        val t = criteria.carbohydrate.perServingG[meal] ?: return TrafficLight.AMBER
        return light(carbs, t.greenMax, t.amberMax)
    }

    fun sodiumLight(recipe: CompiledRecipe): TrafficLight {
        val sodium = recipe.perServing[Nutrient.SODIUM_MG] ?: return TrafficLight.RED
        return sodiumLight(sodium, recipe.servingGrams)
    }

    /** Sodium light for [sodiumMg] eaten in [grams] of food. */
    fun sodiumLight(sodiumMg: Double, grams: Double): TrafficLight {
        val sodium = sodiumMg
        val c = criteria.sodium
        val per100g = if (grams > 0) sodium * 100.0 / grams else Double.MAX_VALUE
        return when {
            per100g >= c.per100gRedFromMg -> TrafficLight.RED
            sodium > c.dailyLimitMg * c.perServingRedShareOfDaily -> TrafficLight.RED
            per100g <= c.per100gGreenMaxMg -> TrafficLight.GREEN
            else -> TrafficLight.AMBER
        }
    }

    /**
     * Carbohydrate light for an amount of a single food (not a whole meal). It uses the snack
     * thresholds, the most cautious ones, or the person's own limit when they have one.
     * The light depends only on the grams of carbohydrate in the amount eaten.
     */
    fun carbLightForAmount(carbsG: Double, profile: HealthProfile? = null): TrafficLight {
        val custom = profile?.customLimits?.maxCarbsPerMealG
        if (custom != null) return if (carbsG <= custom) TrafficLight.GREEN else TrafficLight.RED
        val t = criteria.carbohydrate.perServingG[MealType.SNACK] ?: return TrafficLight.AMBER
        return light(carbsG, t.greenMax, t.amberMax)
    }

    fun fiberLabel(recipe: CompiledRecipe): FiberLabel {
        val fiber = recipe.perServing[Nutrient.FIBER_G] ?: return FiberLabel.NONE
        return when {
            fiber >= criteria.fiber.highPerServingG -> FiberLabel.HIGH
            fiber >= criteria.fiber.sourcePerServingG -> FiberLabel.SOURCE
            else -> FiberLabel.NONE
        }
    }

    fun assess(recipe: CompiledRecipe, meal: MealType, profile: HealthProfile): HealthAssessment {
        val carbs = carbLight(recipe, meal, profile)
        val sodium = sodiumLight(recipe)
        val relevantRed = (profile.hasDiabetes && carbs == TrafficLight.RED) ||
            (profile.hasHypertension && sodium == TrafficLight.RED)
        return HealthAssessment(
            carbs = carbs,
            sodium = sodium,
            fiber = fiberLabel(recipe),
            usesCustomCarbLimit = profile.customLimits.maxCarbsPerMealG != null,
            relevantRed = relevantRed,
        )
    }

    private fun light(value: Double, greenMax: Double, amberMax: Double) = when {
        value <= greenMax -> TrafficLight.GREEN
        value <= amberMax -> TrafficLight.AMBER
        else -> TrafficLight.RED
    }
}
