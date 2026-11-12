package com.velkyss.diabem.core.search

import com.velkyss.diabem.core.model.CompiledRecipe
import com.velkyss.diabem.core.model.RecipeIngredient
import com.velkyss.diabem.core.nutrition.GRAM_UNIT
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

/** An ingredient amount after changing the number of servings. */
data class ScaledIngredient(
    val ingredient: RecipeIngredient,
    /** Quantity rounded to something a person can measure at home. */
    val quantity: Double,
    val grams: Double,
)

/**
 * Changes the number of servings of a recipe. Nutrients per serving do not change;
 * only the amounts to cook do.
 */
object ServingScaler {
    const val MIN_SERVINGS = 1
    const val MAX_SERVINGS = 12

    fun scale(recipe: CompiledRecipe, servings: Int): List<ScaledIngredient> {
        require(servings in MIN_SERVINGS..MAX_SERVINGS) { "servings must be in $MIN_SERVINGS..$MAX_SERVINGS" }
        val factor = servings.toDouble() / recipe.recipe.servings
        return recipe.recipe.ingredients.mapIndexed { i, ing ->
            ScaledIngredient(
                ingredient = ing,
                quantity = roundForKitchen(ing.quantity * factor, ing.unit),
                grams = recipe.ingredientGrams[i] * factor,
            )
        }
    }

    /**
     * Household units are rounded to the nearest quarter (never to zero);
     * grams and millilitres to whole numbers (or 0.1 below 10, e.g. salt).
     */
    fun roundForKitchen(value: Double, unit: String): Double = when {
        unit == GRAM_UNIT || unit == "ml" -> when {
            value < 10 -> maxOf(0.1, round(value * 10) / 10)
            value < 100 -> round(value)
            else -> round(value / 5) * 5
        }
        else -> maxOf(0.25, round(value * 4) / 4)
    }
}

/** Formats quantities the way people say them: "1/2", "1 1/4", "3". */
object QuantityFormatter {
    private val fractions = mapOf(0.25 to "1/4", 0.5 to "1/2", 0.75 to "3/4")

    fun format(value: Double, decimalSeparator: Char = ','): String {
        val whole = floor(value + 1e-9)
        val rest = value - whole
        val fraction = fractions.entries.firstOrNull { abs(it.key - rest) < 0.01 }?.value
        return when {
            rest < 0.01 -> whole.toLong().toString()
            fraction != null && whole == 0.0 -> fraction
            fraction != null -> "${whole.toLong()} $fraction"
            else -> {
                val tenths = round(value * 10) / 10
                if (tenths == floor(tenths)) tenths.toLong().toString() else tenths.toString().replace('.', decimalSeparator)
            }
        }
    }
}
