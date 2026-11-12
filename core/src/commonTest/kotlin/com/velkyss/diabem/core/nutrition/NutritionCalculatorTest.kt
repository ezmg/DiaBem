package com.velkyss.diabem.core.nutrition

import com.velkyss.diabem.core.TestData
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.Nutrients
import com.velkyss.diabem.core.model.RecipeIngredient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NutritionCalculatorTest {
    private val calc = NutritionCalculator(TestData.foods, TestData.measures)

    @Test
    fun convertsHouseholdMeasuresToGrams() {
        assertEquals(120.0, calc.grams("rice", 6.0, "spoon"))
        assertEquals(280.0, calc.grams("beans", 2.0, "ladle"))
        assertEquals(3.5, calc.grams("salt", 3.5, GRAM_UNIT))
        assertNull(calc.grams("rice", 1.0, "ladle"))
    }

    @Test
    fun computesNutrientsPerServing() {
        val c = calc.compile(TestData.recipe())
        // rice 120 g (25 g/100 g) + beans 280 g (14 g/100 g) = 30 + 39.2 = 69.2 g -> 34.6 per serving
        assertEquals(34.6, c.perServing[Nutrient.CARBOHYDRATE_G])
        // sodium: 1.2 + 5.6 + 400 = 406.8 mg -> 203.4 per serving
        assertEquals(203.4, c.perServing[Nutrient.SODIUM_MG])
        assertEquals(401.0, c.totalGrams)
        assertEquals(200.5, c.servingGrams)
        assertEquals(listOf(120.0, 280.0, 1.0), c.ingredientGrams)
        assertTrue(c.incompleteNutrients.isEmpty())
    }

    @Test
    fun countsAddedSugarFromFlaggedFoods() {
        val r = TestData.recipe(
            ingredients = listOf(RecipeIngredient("rice", 5.0, "spoon"), RecipeIngredient("sugar", 2.0, "spoon")),
        )
        assertEquals(12.0, calc.compile(r).addedSugarPerServingG)
    }

    @Test
    fun missingTableValueIsNeverTreatedAsZero() {
        val r = TestData.recipe(
            ingredients = listOf(RecipeIngredient("rice", 5.0, "spoon"), RecipeIngredient("mystery", 50.0, GRAM_UNIT)),
        )
        val c = calc.compile(r)
        assertNull(c.perServing[Nutrient.FIBER_G])
        assertEquals(setOf(Nutrient.FIBER_G), c.incompleteNutrients)
        assertFalse(Nutrient.CARBOHYDRATE_G in c.incompleteNutrients)
    }

    @Test
    fun unknownMeasureFails() {
        val r = TestData.recipe(ingredients = listOf(RecipeIngredient("rice", 1.0, "ladle")))
        assertFailsWith<IllegalArgumentException> { calc.compile(r) }
    }

    @Test
    fun nutrientsPlusKeepsUnknownsUnknown() {
        val a = Nutrients(mapOf(Nutrient.ENERGY_KCAL to 10.0, Nutrient.FIBER_G to 1.0))
        val b = Nutrients(mapOf(Nutrient.ENERGY_KCAL to 5.0))
        val sum = a + b
        assertEquals(15.0, sum[Nutrient.ENERGY_KCAL])
        assertNull(sum[Nutrient.FIBER_G])
    }
}
