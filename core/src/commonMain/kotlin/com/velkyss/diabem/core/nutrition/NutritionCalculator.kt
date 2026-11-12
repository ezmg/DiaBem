package com.velkyss.diabem.core.nutrition

import com.velkyss.diabem.core.model.CompiledRecipe
import com.velkyss.diabem.core.model.Food
import com.velkyss.diabem.core.model.FoodMeasure
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.Nutrients
import com.velkyss.diabem.core.model.Recipe

/** Unit available for every food: plain grams. */
const val GRAM_UNIT = "g"

/**
 * Computes the nutrition of a recipe from composition tables and household measures.
 * Pure function of its inputs: no estimation, no defaults for missing data.
 */
class NutritionCalculator(
    foods: Collection<Food>,
    measures: Collection<FoodMeasure>,
) {
    private val foodsById = foods.associateBy { it.id }
    private val gramsByFoodAndUnit = measures.associate { (it.food to it.unit) to it.grams }

    fun food(id: String): Food? = foodsById[id]

    /** Grams for [quantity] of [unit] of [foodId], or null if the measure is unknown. */
    fun grams(foodId: String, quantity: Double, unit: String): Double? {
        if (unit == GRAM_UNIT) return quantity
        val perUnit = gramsByFoodAndUnit[foodId to unit] ?: return null
        return quantity * perUnit
    }

    /**
     * Compiles [recipe]. Throws [IllegalArgumentException] if a food or a measure is unknown:
     * the validator reports those cases with clear messages before this is called.
     */
    fun compile(recipe: Recipe): CompiledRecipe {
        require(recipe.servings > 0) { "Recipe ${recipe.id}: servings must be positive" }
        val grams = recipe.ingredients.map { ing ->
            requireNotNull(grams(ing.food, ing.quantity, ing.unit)) {
                "Recipe ${recipe.id}: no measure '${ing.unit}' for food '${ing.food}'"
            }
        }
        var total = Nutrients.ZERO
        var addedSugar = 0.0
        recipe.ingredients.forEachIndexed { i, ing ->
            val food = requireNotNull(foodsById[ing.food]) { "Recipe ${recipe.id}: unknown food '${ing.food}'" }
            total += food.per100g.scaled(grams[i] / 100.0)
            if (food.addedSugar) addedSugar += grams[i]
        }
        val incomplete = Nutrient.entries.filterNot { total.isKnown(it) }.toSet()
        return CompiledRecipe(
            recipe = recipe,
            ingredientGrams = grams.map { it.roundTo(1) },
            totalGrams = grams.sum().roundTo(1),
            perServing = Nutrients(total.scaled(1.0 / recipe.servings).values.mapValues { it.value.roundTo(1) }),
            addedSugarPerServingG = (addedSugar / recipe.servings).roundTo(1),
            incompleteNutrients = incomplete,
        )
    }
}

internal fun Double.roundTo(decimals: Int): Double {
    var f = 1.0
    repeat(decimals) { f *= 10 }
    return kotlin.math.round(this * f) / f
}
