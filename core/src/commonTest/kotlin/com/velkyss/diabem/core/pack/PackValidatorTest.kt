package com.velkyss.diabem.core.pack

import com.velkyss.diabem.core.TestData
import com.velkyss.diabem.core.model.RecipeIngredient
import com.velkyss.diabem.core.nutrition.NutritionCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PackValidatorTest {

    private fun errorsFor(vararg ingredients: RecipeIngredient, servings: Int = 2) =
        PackValidator.validateSources(TestData.sources(listOf(TestData.recipe(servings = servings, ingredients = ingredients.toList()))))

    @Test
    fun validDataHasNoErrors() {
        assertEquals(emptyList(), PackValidator.validateSources(TestData.sources()))
    }

    @Test
    fun unknownFoodIsReported() {
        val e = errorsFor(RecipeIngredient("caviar", 1.0, "spoon"))
        assertTrue(e.any { "unknown food 'caviar'" in it }, e.toString())
    }

    @Test
    fun missingMeasureIsReported() {
        val e = errorsFor(RecipeIngredient("rice", 1.0, "ladle"))
        assertTrue(e.any { "no measure 'ladle' for food 'rice'" in it }, e.toString())
    }

    @Test
    fun impossibleQuantitiesAreReported() {
        assertTrue(errorsFor(RecipeIngredient("rice", 0.0, "spoon")).any { "must be positive" in it })
        assertTrue(errorsFor(RecipeIngredient("rice", 100.0, "spoon")).any { "impossible quantity" in it })
        assertTrue(errorsFor(RecipeIngredient("rice", 1.0, "spoon"), servings = 40).any { "servings" in it })
    }

    @Test
    fun duplicateIngredientIsReported() {
        val e = errorsFor(RecipeIngredient("rice", 1.0, "spoon"), RecipeIngredient("rice", 2.0, "spoon"))
        assertTrue(e.any { "listed more than once" in it })
    }

    @Test
    fun compiledChecksCatchImpossibleTotals() {
        val calc = NutritionCalculator(TestData.foods, TestData.measures)
        val salty = calc.compile(TestData.recipe(servings = 1, ingredients = listOf(RecipeIngredient("rice", 5.0, "spoon"), RecipeIngredient("salt", 10.0, "g"))))
        assertTrue(PackValidator.validateCompiled(salty).any { "sodium per serving" in it })
        val tiny = calc.compile(TestData.recipe(servings = 4, ingredients = listOf(RecipeIngredient("rice", 1.0, "spoon"))))
        assertTrue(PackValidator.validateCompiled(tiny).any { "serving weight" in it })
    }
}
