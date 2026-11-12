package com.velkyss.diabem.core.pack

import com.velkyss.diabem.core.model.CompiledRecipe
import com.velkyss.diabem.core.model.Food
import com.velkyss.diabem.core.model.FoodMeasure
import com.velkyss.diabem.core.model.HouseholdUnit
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.PantryCategory
import com.velkyss.diabem.core.model.PantryItem
import com.velkyss.diabem.core.model.Recipe
import com.velkyss.diabem.core.model.Substitution
import com.velkyss.diabem.core.nutrition.GRAM_UNIT

/**
 * Sanity limits. They catch typing mistakes ("100 xícaras"), not health rules.
 * A recipe that breaks one of them is rejected at build time.
 */
object PlausibilityLimits {
    const val MAX_SERVINGS = 12
    const val MAX_QUANTITY_PER_INGREDIENT = 50.0
    const val MAX_INGREDIENT_GRAMS_PER_SERVING = 600.0
    const val MIN_SERVING_GRAMS = 30.0
    const val MAX_SERVING_GRAMS = 900.0
    const val MIN_SERVING_KCAL = 20.0
    const val MAX_SERVING_KCAL = 1200.0
    const val MAX_SERVING_SODIUM_MG = 2000.0
    const val MAX_PREP_MINUTES = 240
    val REQUIRED_NUTRIENTS = setOf(Nutrient.ENERGY_KCAL, Nutrient.CARBOHYDRATE_G, Nutrient.SODIUM_MG)
}

/** Everything the validator needs to check a pack before compiling it. */
data class PackSources(
    val categories: List<PantryCategory>,
    val pantry: List<PantryItem>,
    val foods: List<Food>,
    val units: List<HouseholdUnit>,
    val measures: List<FoodMeasure>,
    val substitutions: List<Substitution>,
    val recipes: List<Recipe>,
)

/** Checks the human-written data. Returns a list of errors; empty means valid. */
object PackValidator {

    private val ID = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")

    fun validateSources(s: PackSources): List<String> {
        val errors = mutableListOf<String>()
        fun dupes(kind: String, ids: List<String>) =
            ids.groupBy { it }.filter { it.value.size > 1 }.keys.forEach { errors += "Duplicate $kind id '$it'" }
        fun badIds(kind: String, ids: List<String>) =
            ids.filterNot { ID.matches(it) }.forEach { errors += "Invalid $kind id '$it' (use lowercase-with-dashes)" }

        dupes("category", s.categories.map { it.id })
        dupes("pantry item", s.pantry.map { it.id })
        dupes("food", s.foods.map { it.id })
        dupes("unit", s.units.map { it.id })
        dupes("recipe", s.recipes.map { it.id })
        badIds("pantry item", s.pantry.map { it.id })
        badIds("food", s.foods.map { it.id })
        badIds("recipe", s.recipes.map { it.id })

        val categoryIds = s.categories.map { it.id }.toSet()
        val pantryIds = s.pantry.map { it.id }.toSet()
        val unitIds = s.units.map { it.id }.toSet() + GRAM_UNIT
        val foodIds = s.foods.map { it.id }.toSet()
        val measureKeys = s.measures.map { it.food to it.unit }.toSet()

        s.pantry.filter { it.category !in categoryIds }
            .forEach { errors += "Pantry item '${it.id}': unknown category '${it.category}'" }
        s.foods.filter { it.pantryItem !in pantryIds }
            .forEach { errors += "Food '${it.id}': unknown pantry item '${it.pantryItem}'" }
        s.foods.forEach { f ->
            PlausibilityLimits.REQUIRED_NUTRIENTS.filterNot { f.per100g.isKnown(it) }
                .forEach { errors += "Food '${f.id}': no data for $it in the source table" }
            f.per100g.values.filter { it.value < 0 }
                .forEach { errors += "Food '${f.id}': negative value for ${it.key}" }
        }
        s.measures.forEach { m ->
            if (m.food !in foodIds) errors += "Measure: unknown food '${m.food}'"
            if (m.unit !in unitIds) errors += "Measure: unknown unit '${m.unit}' for food '${m.food}'"
            if (m.grams <= 0.0) errors += "Measure ${m.food}/${m.unit}: grams must be positive"
        }
        s.measures.groupBy { it.food to it.unit }.filter { it.value.size > 1 }
            .forEach { errors += "Measure ${it.key.first}/${it.key.second} defined more than once" }
        s.substitutions.forEach { sub ->
            if (sub.from !in pantryIds) errors += "Substitution: unknown pantry item '${sub.from}'"
            if (sub.to !in pantryIds) errors += "Substitution: unknown pantry item '${sub.to}'"
            if (sub.from == sub.to) errors += "Substitution: '${sub.from}' replaced by itself"
        }

        val maxByUnit = s.units.associate { it.id to it.maxQuantity }
        s.recipes.forEach { r -> errors += validateRecipe(r, foodIds, unitIds, measureKeys, maxByUnit) }
        return errors
    }

    private fun validateRecipe(
        r: Recipe,
        foodIds: Set<String>,
        unitIds: Set<String>,
        measureKeys: Set<Pair<String, String>>,
        maxByUnit: Map<String, Double>,
    ): List<String> {
        val e = mutableListOf<String>()
        val p = "Recipe '${r.id}'"
        if (r.name.isBlank()) e += "$p: empty name"
        if (r.mealTypes.isEmpty()) e += "$p: no meal type"
        if (r.servings !in 1..PlausibilityLimits.MAX_SERVINGS) e += "$p: impossible number of servings ${r.servings}"
        if (r.prepMinutes !in 1..PlausibilityLimits.MAX_PREP_MINUTES) e += "$p: impossible preparation time ${r.prepMinutes}"
        if (r.ingredients.isEmpty()) e += "$p: no ingredients"
        if (r.steps.isEmpty() || r.steps.any { it.isBlank() }) e += "$p: steps missing or empty"
        r.ingredients.groupBy { it.food }.filter { it.value.size > 1 }
            .forEach { e += "$p: ingredient '${it.key}' listed more than once" }
        r.ingredients.forEach { ing ->
            if (ing.food !in foodIds) e += "$p: unknown food '${ing.food}'"
            if (ing.unit !in unitIds) e += "$p: unknown unit '${ing.unit}'"
            else if (ing.unit != GRAM_UNIT && (ing.food to ing.unit) !in measureKeys && ing.food in foodIds) {
                e += "$p: no measure '${ing.unit}' for food '${ing.food}'"
            }
            if (ing.quantity <= 0.0) e += "$p: quantity of '${ing.food}' must be positive"
            val max = maxByUnit[ing.unit] ?: PlausibilityLimits.MAX_QUANTITY_PER_INGREDIENT
            if (ing.unit != GRAM_UNIT && ing.quantity > max) {
                e += "$p: impossible quantity ${ing.quantity} ${ing.unit} of '${ing.food}'"
            }
        }
        return e
    }

    /** Checks the computed result: catches impossible totals that single fields cannot show. */
    fun validateCompiled(c: CompiledRecipe): List<String> {
        val e = mutableListOf<String>()
        val p = "Recipe '${c.id}'"
        val serving = c.servingGrams
        if (serving < PlausibilityLimits.MIN_SERVING_GRAMS || serving > PlausibilityLimits.MAX_SERVING_GRAMS) {
            e += "$p: impossible serving weight ${serving.toInt()} g"
        }
        c.recipe.ingredients.forEachIndexed { i, ing ->
            if (c.ingredientGrams[i] / c.recipe.servings > PlausibilityLimits.MAX_INGREDIENT_GRAMS_PER_SERVING) {
                e += "$p: impossible amount of '${ing.food}' per serving (${c.ingredientGrams[i].toInt()} g in total)"
            }
        }
        PlausibilityLimits.REQUIRED_NUTRIENTS.filter { it in c.incompleteNutrients }
            .forEach { e += "$p: $it cannot be calculated (missing data in the table)" }
        c.perServing[Nutrient.ENERGY_KCAL]?.let {
            if (it < PlausibilityLimits.MIN_SERVING_KCAL || it > PlausibilityLimits.MAX_SERVING_KCAL) {
                e += "$p: impossible energy per serving ${it.toInt()} kcal"
            }
        }
        c.perServing[Nutrient.SODIUM_MG]?.let {
            if (it > PlausibilityLimits.MAX_SERVING_SODIUM_MG) e += "$p: sodium per serving ${it.toInt()} mg is not acceptable"
        }
        return e
    }
}
