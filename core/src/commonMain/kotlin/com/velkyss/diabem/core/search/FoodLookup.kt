package com.velkyss.diabem.core.search

import com.velkyss.diabem.core.health.HealthEvaluator
import com.velkyss.diabem.core.health.TrafficLight
import com.velkyss.diabem.core.model.CatalogFood
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.Nutrients
import com.velkyss.diabem.core.pack.CountryPack
import com.velkyss.diabem.core.profile.HealthProfile

/** Lower-case text without accents, so "acucar" finds "açúcar". */
fun normalizeText(text: String): String = buildString {
    for (c in text.lowercase()) {
        append(
            when (c) {
                'á', 'à', 'â', 'ã', 'ä' -> 'a'
                'é', 'è', 'ê', 'ë' -> 'e'
                'í', 'ì', 'î', 'ï' -> 'i'
                'ó', 'ò', 'ô', 'õ', 'ö' -> 'o'
                'ú', 'ù', 'û', 'ü' -> 'u'
                'ç' -> 'c'
                'ñ' -> 'n'
                else -> c
            },
        )
    }
}

/** What the app shows for an amount of one food. */
data class FoodAssessment(
    val grams: Double,
    /** Nutrients in [grams] of the food. Unknown values stay unknown. */
    val nutrients: Nutrients,
    /** Null when the table has no carbohydrate value for this food. */
    val carbs: TrafficLight?,
    /** Null when the table has no sodium value for this food. */
    val sodium: TrafficLight?,
    /** True when a light that matters for the person's condition is red. */
    val relevantRed: Boolean,
)

/**
 * Looks up single foods of the composition table and applies the traffic lights to the
 * amount the person plans to eat. It informs; it never says "you can" or "you cannot".
 */
class FoodLookup(private val pack: CountryPack) {
    private val evaluator = HealthEvaluator(pack.healthCriteria)
    // Everyday names first, so a search that starts with one of them ranks at the top.
    private val index = pack.catalog.map { normalizeText((it.aliases + it.name).joinToString(" | ")) to it }

    fun food(id: String): CatalogFood? = pack.catalog.firstOrNull { it.id == id }

    /**
     * Foods whose name contains every word of [query]. Names that start with the first word
     * come first; then alphabetical order.
     */
    fun search(query: String, limit: Int = DEFAULT_LIMIT): List<CatalogFood> {
        val words = normalizeText(query).split(' ', ',').filter { it.isNotBlank() }
        if (words.isEmpty() || words.all { it.length < MIN_QUERY_LENGTH }) return emptyList()
        return index.filter { (name, _) -> words.all { it in name } }
            .sortedWith(compareBy<Pair<String, CatalogFood>> { (name, _) -> name.split(" | ").none { it.startsWith(words.first()) } }.thenBy { it.first })
            .take(limit)
            .map { it.second }
    }

    fun assess(food: CatalogFood, grams: Double, profile: HealthProfile?): FoodAssessment {
        require(grams > 0) { "grams must be positive" }
        val nutrients = Nutrients(food.per100g.scaled(grams / 100.0).values.mapValues { round1(it.value) })
        val carbs = nutrients[Nutrient.CARBOHYDRATE_G]?.let { evaluator.carbLightForAmount(it, profile) }
        val sodium = nutrients[Nutrient.SODIUM_MG]?.let { evaluator.sodiumLight(it, grams) }
        val relevantRed = (profile?.hasDiabetes == true && carbs == TrafficLight.RED) ||
            (profile?.hasHypertension == true && sodium == TrafficLight.RED)
        return FoodAssessment(grams, nutrients, carbs, sodium, relevantRed)
    }

    private fun round1(v: Double) = kotlin.math.round(v * 10) / 10

    companion object {
        const val DEFAULT_LIMIT = 30
        const val MIN_QUERY_LENGTH = 2
    }
}
