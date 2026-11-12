package com.velkyss.diabem.core.search

import com.velkyss.diabem.core.health.HealthAssessment
import com.velkyss.diabem.core.health.HealthEvaluator
import com.velkyss.diabem.core.health.TrafficLight
import com.velkyss.diabem.core.model.CompiledRecipe
import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.model.Substitution
import com.velkyss.diabem.core.pack.CountryPack
import com.velkyss.diabem.core.profile.HealthProfile

data class SearchQuery(
    val meal: MealType,
    /** Pantry item ids the person has at home. */
    val available: Set<String>,
    val profile: HealthProfile,
    val maxResults: Int = DEFAULT_MAX_RESULTS,
) {
    companion object {
        const val DEFAULT_MAX_RESULTS = 5
    }
}

data class RecipeMatch(
    val recipe: CompiledRecipe,
    /** Needed pantry items the person has. */
    val have: List<String>,
    /** Needed pantry items the person does not have (and cannot replace). */
    val missing: List<String>,
    /** Missing items that can be replaced by something the person has. */
    val substitutions: List<Substitution>,
    /** 0..1: share of needed items covered (a substitution counts half). */
    val coverage: Double,
    val health: HealthAssessment,
    /** Spoons of rice per serving, when the recipe has rice measured in spoons. */
    val riceSpoonsPerServing: Double?,
    /** True when [riceSpoonsPerServing] is above the person's own limit. */
    val exceedsRiceLimit: Boolean,
)

/**
 * Finds recipes for a meal using what the person has at home.
 *
 * Order: recipes that are fine for the profile first, then fewer missing items, then better
 * coverage, then the healthier light for the person's condition. Recipes with a red light for
 * the person's condition are only shown when there are not enough other recipes, and they keep
 * the red flag so the UI can warn about them.
 */
class RecipeSearch(private val pack: CountryPack) {

    private val evaluator = HealthEvaluator(pack.healthCriteria)
    private val pantryByFood = pack.foods.associate { it.id to it.pantryItem }
    private val staples = pack.pantry.filter { it.staple }.map { it.id }.toSet()
    private val substitutionsByItem = pack.substitutions.groupBy { it.from }

    /** Pantry items a recipe needs, without optional ingredients and kitchen staples. */
    fun neededItems(recipe: CompiledRecipe): List<String> =
        recipe.recipe.ingredients.filterNot { it.optional }
            .mapNotNull { pantryByFood[it.food] }
            .filterNot { it in staples }
            .distinct()

    fun match(recipe: CompiledRecipe, query: SearchQuery): RecipeMatch {
        val needed = neededItems(recipe)
        val owned = query.available + staples
        val have = needed.filter { it in owned }
        val notOwned = needed.filterNot { it in owned }
        val subs = notOwned.mapNotNull { item -> substitutionsByItem[item]?.firstOrNull { it.to in owned } }
        val missing = notOwned.filterNot { item -> subs.any { it.from == item } }
        val coverage = if (needed.isEmpty()) 1.0 else (have.size + 0.5 * subs.size) / needed.size
        val rice = riceSpoonsPerServing(recipe)
        val limit = query.profile.customLimits.maxRiceSpoonsPerMeal
        return RecipeMatch(
            recipe = recipe,
            have = have,
            missing = missing,
            substitutions = subs,
            coverage = coverage,
            health = evaluator.assess(recipe, query.meal, query.profile),
            riceSpoonsPerServing = rice,
            exceedsRiceLimit = rice != null && limit != null && rice > limit,
        )
    }

    fun search(query: SearchQuery): List<RecipeMatch> {
        val candidates = pack.recipes
            .filter { query.meal in it.recipe.mealTypes }
            .map { match(it, query) }
            .filter { m -> query.available.isEmpty() || m.have.any { it in query.available } }
            .filter { m -> query.available.isEmpty() || m.missing.size <= maxMissing(m) }
            .sortedWith(order(query))
        val fine = candidates.filterNot { it.health.relevantRed || it.exceedsRiceLimit }
        val flagged = candidates.filter { it.health.relevantRed || it.exceedsRiceLimit }
        val minResults = minOf(MIN_RESULTS, query.maxResults)
        val result = if (fine.size >= minResults) fine else fine + flagged.take(minResults - fine.size)
        return result.take(query.maxResults)
    }

    private fun maxMissing(m: RecipeMatch): Int {
        val needed = m.have.size + m.missing.size + m.substitutions.size
        return maxOf(MAX_MISSING_FLOOR, needed / 2)
    }

    private fun order(q: SearchQuery): Comparator<RecipeMatch> =
        compareBy<RecipeMatch> { it.health.relevantRed || it.exceedsRiceLimit }
            .thenBy { it.missing.size }
            .thenByDescending { it.coverage }
            .thenBy { if (q.profile.hasDiabetes) it.health.carbs.ordinal else 0 }
            .thenBy { if (q.profile.hasHypertension) it.health.sodium.ordinal else 0 }
            .thenBy { it.recipe.recipe.name }

    private fun riceSpoonsPerServing(recipe: CompiledRecipe): Double? {
        val spoons = recipe.recipe.ingredients
            .filter { pantryByFood[it.food] in pack.ricePantryItems && it.unit == RICE_SPOON_UNIT }
            .sumOf { it.quantity }
        return if (spoons > 0) spoons / recipe.recipe.servings else null
    }

    companion object {
        const val MIN_RESULTS = 3
        const val MAX_MISSING_FLOOR = 2
        const val RICE_SPOON_UNIT = "colher_sopa"
    }
}

/** Convenience for the UI: the lights as a pair. */
val RecipeMatch.lights: Pair<TrafficLight, TrafficLight> get() = health.carbs to health.sodium
