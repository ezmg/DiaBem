package com.velkyss.diabem.core.model

import kotlinx.serialization.Serializable

/** Where a value comes from, so every number in the app can be traced back. */
@Serializable
data class SourceRef(
    /** Id of an entry in the pack's source list, e.g. "taco4" or "ibge-pof-2008". */
    val source: String,
    /** Identifier inside that source (row, code, page...). */
    val reference: String,
)

/** A food from an official composition table, with nutrients per 100 g of edible part. */
@Serializable
data class Food(
    val id: String,
    val name: String,
    /** Pantry item the user picks to say they have this food. */
    val pantryItem: String,
    val per100g: Nutrients,
    /** True when this food counts as added sugar (sugar, honey...). */
    val addedSugar: Boolean = false,
    val origin: SourceRef,
    /** Values missing in the main table and taken from another named source. */
    val supplements: Map<Nutrient, SourceRef> = emptyMap(),
)

/** A way to measure a catalogue food at home: one [unit] weighs [grams]. */
@Serializable
data class CatalogPortion(val unit: String, val grams: Double)

/**
 * Any food of the official composition table, for the "look up a food" screen.
 * Unlike [Food], it does not need to be used by a recipe.
 */
@Serializable
data class CatalogFood(
    /** Row id in the composition table. */
    val id: String,
    /** Name exactly as written in the table. */
    val name: String,
    val per100g: Nutrients,
    /** Household measures known for this food (may be empty: then only grams are offered). */
    val portions: List<CatalogPortion> = emptyList(),
    /** Everyday names people use for this food ("Goiabada"), so the search finds it. */
    val aliases: List<String> = emptyList(),
) {
    /** The everyday name when there is one, otherwise the table name. */
    val displayName: String get() = aliases.firstOrNull() ?: name
}

/** Something the user can have at home and tick in the ingredient list (e.g. "Arroz"). */
@Serializable
data class PantryItem(
    val id: String,
    val name: String,
    val category: String,
    /** Basic items assumed to be in every kitchen (water, salt, oil). */
    val staple: Boolean = false,
)

@Serializable
data class PantryCategory(
    val id: String,
    val name: String,
    /** Icon key resolved by the UI. */
    val icon: String,
)

/** A household measure such as "colher de sopa". */
@Serializable
data class HouseholdUnit(
    val id: String,
    val singular: String,
    val plural: String,
    /** Largest believable quantity of this unit in one recipe (catches typing mistakes). */
    val maxQuantity: Double = 50.0,
)

/** Grams of one [unit] of one [food], with the source of the equivalence. */
@Serializable
data class FoodMeasure(
    val food: String,
    val unit: String,
    val grams: Double,
    val origin: SourceRef,
)

/** "If you don't have [from], you can use [to]". */
@Serializable
data class Substitution(
    val from: String,
    val to: String,
)
