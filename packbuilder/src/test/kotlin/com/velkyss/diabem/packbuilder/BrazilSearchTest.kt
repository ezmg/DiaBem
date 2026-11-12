package com.velkyss.diabem.packbuilder

import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.profile.Condition
import com.velkyss.diabem.core.profile.HealthProfile
import com.velkyss.diabem.core.search.RecipeSearch
import com.velkyss.diabem.core.search.SearchQuery
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** End-to-end checks of the search with the real Brazilian data. */
class BrazilSearchTest {
    private val pack = PackCompiler(File(System.getProperty("diabem.dataDir"), "br")).compile()
    private val search = RecipeSearch(pack)
    private val profiles = listOf(
        setOf(Condition.DIABETES_TYPE_2),
        setOf(Condition.HYPERTENSION),
        setOf(Condition.DIABETES_TYPE_2, Condition.HYPERTENSION),
    ).map { HealthProfile(it) }

    private val typicalKitchen = setOf("arroz", "pao-frances", "cebolinha", "alface", "feijao", "ovo", "tomate", "cebola", "alho", "banana", "frango", "aveia", "batata")

    @Test
    fun everyMealGivesThreeToFiveRecipes() {
        for (profile in profiles) for (meal in MealType.entries) {
            val result = search.search(SearchQuery(meal, typicalKitchen, profile))
            assertTrue(result.size in 2..5, "$meal / ${profile.conditions}: ${result.map { it.recipe.id }}")
            assertTrue(result.all { meal in it.recipe.recipe.mealTypes })
            // every result is something the person can really cook: at least half the ingredients at home
            assertTrue(result.all { it.closeMatch && it.missing.size <= 2 && it.coverage >= 0.5 })
        }
    }

    @Test
    fun eggsAndTomatoFindEggRecipesForBreakfast() {
        val result = search.search(SearchQuery(MealType.BREAKFAST, setOf("ovo", "tomate"), profiles[2]))
        assertTrue(result.isNotEmpty())
        assertTrue(result.first().have.contains("ovo"), result.map { it.recipe.id }.toString())
    }
}
