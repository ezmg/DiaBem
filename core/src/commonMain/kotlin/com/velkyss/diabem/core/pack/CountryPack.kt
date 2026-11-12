package com.velkyss.diabem.core.pack

import com.velkyss.diabem.core.health.HealthCriteria
import com.velkyss.diabem.core.model.CompiledRecipe
import com.velkyss.diabem.core.model.Food
import com.velkyss.diabem.core.model.FoodMeasure
import com.velkyss.diabem.core.model.HouseholdUnit
import com.velkyss.diabem.core.model.PantryCategory
import com.velkyss.diabem.core.model.PantryItem
import com.velkyss.diabem.core.model.Substitution
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A data source with its licence, shown in the app's "about" screen. */
@Serializable
data class DataSource(
    val id: String,
    val citation: String,
    val url: String? = null,
    val license: String,
)

/**
 * Everything the app needs for one country, produced by the pack builder at build time.
 * The app only reads this file; it never computes nutrients from raw tables.
 */
@Serializable
data class CountryPack(
    val formatVersion: Int = FORMAT_VERSION,
    val id: String,
    val locale: String,
    val sources: List<DataSource>,
    val categories: List<PantryCategory>,
    val pantry: List<PantryItem>,
    val foods: List<Food>,
    val units: List<HouseholdUnit>,
    val measures: List<FoodMeasure>,
    val substitutions: List<Substitution>,
    val healthCriteria: HealthCriteria,
    val recipes: List<CompiledRecipe>,
) {
    companion object {
        const val FORMAT_VERSION = 1

        val json = Json {
            ignoreUnknownKeys = false
            encodeDefaults = true
            prettyPrint = false
        }

        fun decode(text: String): CountryPack = json.decodeFromString(serializer(), text)
    }

    fun encode(): String = json.encodeToString(serializer(), this)
}
