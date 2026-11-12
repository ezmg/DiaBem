package com.velkyss.diabem.core.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeIngredient(
    val food: String,
    val quantity: Double,
    val unit: String,
    /** Extra text shown after the ingredient, e.g. "picada". */
    val note: String? = null,
    /** Optional ingredients do not count as missing in the search. */
    val optional: Boolean = false,
)

/** A recipe as written by a person in the data folder. */
@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val mealTypes: List<MealType>,
    val servings: Int,
    val prepMinutes: Int,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
    val reviewedByNutritionist: Boolean = false,
    val tips: List<String> = emptyList(),
)

/** A recipe after the build step: same content plus computed nutrition. */
@Serializable
data class CompiledRecipe(
    val recipe: Recipe,
    /** Grams of each ingredient, in the same order as [Recipe.ingredients]. */
    val ingredientGrams: List<Double>,
    val totalGrams: Double,
    val perServing: Nutrients,
    /** Grams of added sugar per serving (from ingredients flagged as added sugar). */
    val addedSugarPerServingG: Double,
    /** Nutrients that have no data in the source table for at least one ingredient. */
    val incompleteNutrients: Set<Nutrient> = emptySet(),
) {
    val id: String get() = recipe.id
    val servingGrams: Double get() = totalGrams / recipe.servings
}
