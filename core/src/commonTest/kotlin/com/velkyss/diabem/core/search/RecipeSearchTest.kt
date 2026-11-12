package com.velkyss.diabem.core.search

import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.profile.Condition
import com.velkyss.diabem.core.profile.CustomLimits
import com.velkyss.diabem.core.profile.HealthProfile
import com.velkyss.diabem.core.search.SearchFixtures.compiled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecipeSearchTest {
    private val search = RecipeSearch(SearchFixtures.pack)
    private val diabetes = HealthProfile(setOf(Condition.DIABETES_TYPE_2))
    private val hypertension = HealthProfile(setOf(Condition.HYPERTENSION))
    private val both = HealthProfile(setOf(Condition.DIABETES_TYPE_2, Condition.HYPERTENSION))

    private fun ids(meal: MealType, have: Set<String>, profile: HealthProfile = both, max: Int = 5) =
        search.search(SearchQuery(meal, have, profile, max)).map { it.recipe.id }

    @Test
    fun onlyRecipesForTheChosenMeal() {
        val result = ids(MealType.SNACK, setOf("tomato", "cheese", "onion"))
        assertTrue(result.all { SearchFixtures.compiled(it).recipe.mealTypes.contains(MealType.SNACK) })
    }

    @Test
    fun staplesAndOptionalIngredientsAreNotNeeded() {
        val needed = search.neededItems(compiled("chicken-rice"))
        assertEquals(listOf("chicken", "rice", "tomato"), needed)
    }

    @Test
    fun fullMatchComesFirst() {
        val result = ids(MealType.LUNCH, setOf("chicken", "rice", "tomato"))
        assertEquals("chicken-rice", result.first())
    }

    @Test
    fun missingItemsAndSubstitutionsAreReported() {
        val m = search.match(compiled("fish-tomato"), SearchQuery(MealType.LUNCH, setOf("chicken", "tomato"), both))
        assertEquals(listOf("tomato"), m.have)
        assertEquals(listOf("onion"), m.missing)
        assertEquals("fish", m.substitutions.single().from)
        assertEquals("chicken", m.substitutions.single().to)
        // (1 have + 0.5 substitution) / 3 needed
        assertEquals(0.5, m.coverage)
    }

    @Test
    fun recipesWithoutAnyOwnedIngredientAreHidden() {
        val result = ids(MealType.LUNCH, setOf("onion"))
        assertFalse("chicken-rice" in result)
        assertTrue("fish-tomato" in result)
    }

    @Test
    fun redForTheConditionGoesLastOrIsHidden() {
        val have = setOf("chicken", "rice", "tomato", "beans", "cheese", "fish", "onion")
        val forDiabetes = ids(MealType.LUNCH, have, diabetes)
        assertTrue("big-rice" !in forDiabetes, "big-rice should be hidden when enough other recipes exist: $forDiabetes")
        val forHypertension = ids(MealType.LUNCH, have, hypertension)
        assertTrue("salty-cheese" !in forHypertension, forHypertension.toString())
        // for hypertension only, the big rice plate is not a problem
        assertTrue("big-rice" in forHypertension, forHypertension.toString())
    }

    @Test
    fun redRecipesFillInWhenThereAreTooFewResults() {
        val result = search.search(SearchQuery(MealType.LUNCH, setOf("beans", "rice"), diabetes))
        val bigRice = result.single { it.recipe.id == "big-rice" }
        assertTrue(bigRice.health.relevantRed)
        assertEquals("big-rice", result.last().recipe.id)
    }

    @Test
    fun riceSpoonLimitFromTheNutritionist() {
        val profile = HealthProfile(setOf(Condition.DIABETES_TYPE_2), CustomLimits(maxRiceSpoonsPerMeal = 2))
        val m = search.match(compiled("chicken-rice"), SearchQuery(MealType.LUNCH, setOf("rice"), profile))
        assertEquals(3.0, m.riceSpoonsPerServing)
        assertTrue(m.exceedsRiceLimit)
        val ok = search.match(compiled("chicken-rice"), SearchQuery(MealType.LUNCH, setOf("rice"), diabetes))
        assertFalse(ok.exceedsRiceLimit)
    }

    @Test
    fun resultsAreLimited() {
        val all = setOf("chicken", "rice", "tomato", "beans", "cheese", "fish", "onion")
        assertTrue(ids(MealType.LUNCH, all, hypertension, max = 2).size <= 2)
    }

    @Test
    fun emptyPantryShowsHealthyRecipesForTheMeal() {
        val result = search.search(SearchQuery(MealType.LUNCH, emptySet(), both))
        assertEquals(setOf("chicken-rice", "fish-tomato"), result.take(2).map { it.recipe.id }.toSet())
        // only 2 healthy lunch recipes exist, so one flagged recipe fills the minimum of 3
        assertEquals(3, result.size)
        assertTrue(result.last().health.relevantRed)
    }

    @Test
    fun searchIsDeterministic() {
        val have = setOf("chicken", "tomato", "onion")
        assertEquals(ids(MealType.LUNCH, have), ids(MealType.LUNCH, have))
    }

    @Test
    fun searchAllStartsWithTheShortListAndThenShowsEveryRecipeOfTheMeal() {
        val query = SearchQuery(MealType.LUNCH, setOf("chicken", "rice", "tomato"), hypertension)
        val short = search.search(query).map { it.recipe.id }
        val all = search.searchAll(query).map { it.recipe.id }
        assertEquals(short, all.take(short.size))
        assertEquals(setOf("chicken-rice", "fish-tomato", "big-rice", "salty-cheese"), all.toSet())
        assertEquals(all.size, all.toSet().size)
        // the recipe that is red for this person is still there, but flagged
        assertTrue(search.searchAll(query).single { it.recipe.id == "salty-cheese" }.health.relevantRed)
    }

    @Test
    fun moreCarbsPreferencePutsHigherCarbRecipesFirstButRedOnesStayLast() {
        val have = setOf("chicken", "rice", "tomato", "fish", "onion", "beans")
        val normal = search.searchAll(SearchQuery(MealType.LUNCH, have, diabetes))
        val more = search.searchAll(SearchQuery(MealType.LUNCH, have, diabetes, carbPreference = CarbPreference.MORE_CARBS))
        val fine = more.filterNot { it.health.relevantRed }.map { it.recipe.perServing[com.velkyss.diabem.core.model.Nutrient.CARBOHYDRATE_G]!! }
        assertEquals(fine.sortedDescending(), fine)
        // big-rice is above the red limit for diabetes: never promoted
        assertTrue(more.indexOfFirst { it.recipe.id == "big-rice" } > more.indexOfFirst { it.recipe.id == "chicken-rice" })
        assertEquals(normal.map { it.recipe.id }.toSet(), more.map { it.recipe.id }.toSet())
    }
}
