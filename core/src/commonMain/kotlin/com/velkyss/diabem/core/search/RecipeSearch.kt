package com.velkyss.diabem.core.search

import com.velkyss.diabem.core.health.HealthAssessment
import com.velkyss.diabem.core.health.HealthEvaluator
import com.velkyss.diabem.core.health.TrafficLight
import com.velkyss.diabem.core.model.CompiledRecipe
import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.Substitution
import com.velkyss.diabem.core.pack.CountryPack
import com.velkyss.diabem.core.profile.HealthProfile

/** How the person wants the results ordered. */
enum class CarbPreference {
    /** Default order: best ingredient match first. */
    DEFAULT,

    /**
     * Recipes with more carbohydrates first (for example, when the person's nutritionist asked
     * for more carbohydrates at some meal). Recipes above the red limit still go last.
     */
    MORE_CARBS,
}

data class SearchQuery(
    val meal: MealType,
    /** Pantry item ids the person has at home. */
    val available: Set<String>,
    val profile: HealthProfile,
    val maxResults: Int = DEFAULT_MAX_RESULTS,
    val carbPreference: CarbPreference = CarbPreference.DEFAULT,
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
    /**
     * True when the person can really cook this with what they have: at least half of the
     * needed items at home and at most [RecipeSearch.MAX_MISSING] to buy. Other recipes are
     * only "ideas" and are never shown in the first list.
     */
    val closeMatch: Boolean,
)

/**
 * Finds recipes for a meal using what the person has at home. Only recipes the person can
 * really cook (see [RecipeMatch.closeMatch]) are results; the rest are offered apart as ideas.
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
            closeMatch = query.available.isEmpty() ||
                (have.any { it in query.available } && missing.size <= MAX_MISSING && coverage >= MIN_COVERAGE),
        )
    }

    /** The first, short list: 3 to [SearchQuery.maxResults] recipes. */
    fun search(query: SearchQuery): List<RecipeMatch> {
        val candidates = matching(query)
        val fine = candidates.filterNot { it.flagged }
        val flagged = candidates.filter { it.flagged }
        val minResults = minOf(MIN_RESULTS, query.maxResults)
        val result = if (fine.size >= minResults) fine else fine + flagged.take(minResults - fine.size)
        return result.take(query.maxResults)
    }

    /**
     * Every recipe for the meal, for "show more recipes": first the short list of [search],
     * then the other recipes that match the ingredients, then the rest of the meal's recipes.
     * Flagged recipes keep their flag so the UI can warn about them.
     */
    fun searchAll(query: SearchQuery): List<RecipeMatch> {
        val first = search(query)
        val everything = pack.recipes
            .filter { query.meal in it.recipe.mealTypes }
            .map { match(it, query) }
            .sortedWith(order(query))
        val matchingIds = matching(query).map { it.recipe.id }.toSet()
        val shown = first.map { it.recipe.id }.toSet()
        val rest = everything.filterNot { it.recipe.id in shown }
        return first + rest.filter { it.recipe.id in matchingIds } + rest.filterNot { it.recipe.id in matchingIds }
    }

    private fun matching(query: SearchQuery): List<RecipeMatch> = pack.recipes
        .filter { query.meal in it.recipe.mealTypes }
        .map { match(it, query) }
        .filter { it.closeMatch }
        .sortedWith(order(query))

    private val RecipeMatch.flagged: Boolean get() = health.relevantRed || exceedsRiceLimit

    private fun order(q: SearchQuery): Comparator<RecipeMatch> =
        compareBy<RecipeMatch> { it.flagged }
            .thenByDescending {
                if (q.carbPreference == CarbPreference.MORE_CARBS) it.recipe.perServing[Nutrient.CARBOHYDRATE_G] ?: 0.0 else 0.0
            }
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
        const val MAX_MISSING = 2
        const val MIN_COVERAGE = 0.5
        const val RICE_SPOON_UNIT = "colher_sopa"
    }
}

/** Convenience for the UI: the lights as a pair. */
val RecipeMatch.lights: Pair<TrafficLight, TrafficLight> get() = health.carbs to health.sodium
