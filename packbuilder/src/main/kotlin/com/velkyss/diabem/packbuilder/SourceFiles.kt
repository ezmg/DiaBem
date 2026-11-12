package com.velkyss.diabem.packbuilder

import com.velkyss.diabem.core.model.HouseholdUnit
import com.velkyss.diabem.core.model.PantryCategory
import com.velkyss.diabem.core.model.PantryItem
import com.velkyss.diabem.core.pack.DataSource
import kotlinx.serialization.Serializable

/** data/<country>/pack.json */
@Serializable
data class PackFile(
    val id: String,
    val locale: String,
    val sources: List<DataSource>,
    /** Pantry items counted by the "maximum spoons of rice per meal" limit. */
    val ricePantryItems: Set<String> = emptySet(),
)

/** data/<country>/pantry.json */
@Serializable
data class PantryFile(
    val categories: List<PantryCategory>,
    val items: List<PantryItem>,
)

/** A value that the main table leaves empty, taken from another declared source. */
@Serializable
data class SupplementValue(
    val value: Double,
    val source: String,
    val reference: String,
)

/** One entry of data/<country>/foods.json: a link to a row of the composition table. */
@Serializable
data class FoodEntry(
    val id: String,
    val name: String,
    val pantryItem: String,
    /** Row id in the composition table (TACO number for Brazil). Null only for [nutrientFree] foods. */
    val tableId: String? = null,
    val addedSugar: Boolean = false,
    /**
     * Fills ONLY nutrients that the table leaves empty ("not analysed").
     * Never overrides a value that exists in the table.
     */
    val supplements: Map<com.velkyss.diabem.core.model.Nutrient, SupplementValue> = emptyMap(),
    /** Foods with no nutrients by definition (drinking water). Needs [nutrientFreeSource]. */
    val nutrientFree: Boolean = false,
    val nutrientFreeSource: String? = null,
)

/** data/<country>/foods.json */
@Serializable
data class FoodsFile(
    /** Source id of the composition table, declared in pack.json. */
    val table: String,
    /** CSV file with the table, relative to the country folder. */
    val tableFile: String,
    val foods: List<FoodEntry>,
)

/** Reference to one row of the official household-measures table. */
@Serializable
data class OfficialMeasureRef(
    val food: String,
    val measure: String,
    val preparation: String = "99",
)

/**
 * One household measure for one food. Either [official] (looked up in the official table)
 * or [grams] with a [source] and [reference] that explain where the number comes from.
 */
@Serializable
data class MeasureEntry(
    val food: String,
    val unit: String,
    val official: OfficialMeasureRef? = null,
    val grams: Double? = null,
    val source: String? = null,
    val reference: String? = null,
)

/** data/<country>/measures.json */
@Serializable
data class MeasuresFile(
    /** Source id of the official measures table, declared in pack.json. */
    val table: String,
    val tableFile: String,
    val units: List<HouseholdUnit>,
    val measures: List<MeasureEntry>,
)

@Serializable
data class SubstitutionsFile(
    val substitutions: List<com.velkyss.diabem.core.model.Substitution>,
)
