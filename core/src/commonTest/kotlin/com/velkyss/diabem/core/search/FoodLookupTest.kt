package com.velkyss.diabem.core.search

import com.velkyss.diabem.core.health.TrafficLight
import com.velkyss.diabem.core.model.CatalogFood
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.Nutrients
import com.velkyss.diabem.core.profile.Condition
import com.velkyss.diabem.core.profile.CustomLimits
import com.velkyss.diabem.core.profile.HealthProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FoodLookupTest {
    private fun food(id: String, name: String, carbs: Double?, sodium: Double?) = CatalogFood(
        id, name,
        Nutrients(buildMap { put(Nutrient.ENERGY_KCAL, 100.0); carbs?.let { put(Nutrient.CARBOHYDRATE_G, it) }; sodium?.let { put(Nutrient.SODIUM_MG, it) } }),
    )

    private val bread = food("1", "Pão, trigo, francês", 58.0, 648.0)
    private val lookup = FoodLookup(
        SearchFixtures.pack.copy(
            catalog = listOf(
                bread,
                food("2", "Pão, de queijo, assado", 34.0, 773.0),
                food("3", "Torrada, pão francês", 75.0, 829.0),
                food("4", "Açúcar, refinado", 99.5, 12.0),
                food("5", "Lingüiça, frango, frita", 0.0, 1374.0),
                food("6", "Alimento sem dados", null, null),
            ),
        ),
    )
    private val diabetes = HealthProfile(setOf(Condition.DIABETES_TYPE_2))
    private val hypertension = HealthProfile(setOf(Condition.HYPERTENSION))

    @Test
    fun searchIgnoresAccentsAndCase() {
        assertEquals(listOf("4"), lookup.search("ACUCAR").map { it.id })
        assertEquals(listOf("5"), lookup.search("linguica").map { it.id })
    }

    @Test
    fun namesStartingWithTheWordComeFirst() {
        assertEquals(listOf("2", "1", "3"), lookup.search("pao").map { it.id })
    }

    @Test
    fun everyWordMustMatch() {
        assertEquals(listOf("1", "3"), lookup.search("pão francês").map { it.id })
        assertEquals(emptyList(), lookup.search("pão banana"))
        assertEquals(emptyList(), lookup.search("p"))
        assertEquals(emptyList(), lookup.search("  "))
    }

    @Test
    fun theLightDependsOnTheAmountEaten() {
        // fixture thresholds for carbs: green up to 45 g, amber up to 60 g
        assertEquals(TrafficLight.GREEN, lookup.assess(bread, 50.0, diabetes).carbs) // 29 g
        assertEquals(TrafficLight.AMBER, lookup.assess(bread, 100.0, diabetes).carbs) // 58 g
        assertEquals(TrafficLight.RED, lookup.assess(bread, 150.0, diabetes).carbs) // 87 g
        assertEquals(29.0, lookup.assess(bread, 50.0, diabetes).nutrients[Nutrient.CARBOHYDRATE_G])
    }

    @Test
    fun personalCarbLimitIsUsed() {
        val strict = HealthProfile(setOf(Condition.DIABETES_TYPE_2), CustomLimits(maxCarbsPerMealG = 20.0))
        assertEquals(TrafficLight.RED, lookup.assess(bread, 50.0, strict).carbs)
    }

    @Test
    fun saltyFoodIsRedAndFlaggedOnlyForHypertension() {
        val sausage = lookup.food("5")!!
        val forHypertension = lookup.assess(sausage, 50.0, hypertension)
        assertEquals(TrafficLight.RED, forHypertension.sodium)
        assertEquals(TrafficLight.GREEN, forHypertension.carbs)
        assertTrue(forHypertension.relevantRed)
        assertFalse(lookup.assess(sausage, 50.0, diabetes).relevantRed)
    }

    @Test
    fun missingTableValuesGiveNoLightInsteadOfAGuess() {
        val a = lookup.assess(lookup.food("6")!!, 100.0, diabetes)
        assertNull(a.carbs)
        assertNull(a.sodium)
        assertFalse(a.relevantRed)
    }

    @Test
    fun everydayNamesAreFoundAndRankFirst() {
        val jam = CatalogFood("7", "Goiaba, doce em pasta", bread.per100g, aliases = listOf("Goiabada"))
        val fruit = CatalogFood("8", "Goiaba, vermelha, crua", bread.per100g)
        val withAliases = FoodLookup(SearchFixtures.pack.copy(catalog = listOf(fruit, jam, bread)))
        assertEquals(listOf("7"), withAliases.search("goiabada").map { it.id })
        // "goiaba" matches both; the table name is still searchable
        assertEquals(setOf("7", "8"), withAliases.search("goiaba").map { it.id }.toSet())
        assertEquals("Goiabada", jam.displayName)
        assertEquals("Goiaba, vermelha, crua", fruit.displayName)
    }
}
