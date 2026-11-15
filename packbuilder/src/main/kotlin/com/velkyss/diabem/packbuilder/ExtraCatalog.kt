package com.velkyss.diabem.packbuilder

import com.velkyss.diabem.core.model.CatalogFood
import com.velkyss.diabem.core.model.CatalogPortion
import com.velkyss.diabem.core.model.HouseholdUnit
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.Nutrients
import java.io.File
import java.text.Normalizer

/**
 * Adds the foods of a second, broader composition table (for Brazil: the IBGE POF 2008-2009
 * table) to the food lookup. Rules:
 * - A row that the extra table itself took from the main table is NOT added again: its name
 *   becomes an everyday name of the main-table food, and its household measures are reused.
 * - Preparations of a food with exactly the same values are shown as one entry.
 * - Sodium is the food's own sodium plus the estimated salt of preparation, kept apart too.
 */
class ExtraCatalog(private val countryDir: File, private val config: CatalogFile, private val measuresFile: String) {

    class Result(val foods: List<CatalogFood>, val units: List<HouseholdUnit>, val mainTableAliases: Map<String, List<String>>, val mainTablePortions: Map<String, List<CatalogPortion>>)

    private data class Row(val code: String, val food: String, val prepCode: String, val prep: String, val values: Map<String, Double?>, val sourceCode: String, val sourceDesc: String)

    private fun unitId(code: String) = "ibge-$code"

    /** [mainTableNames]: everyday names already given by hand to main-table foods; those foods win. */
    fun build(mainTable: TacoTable, mainTableNames: Set<String>, errors: MutableList<String>): Result {
        val taken = mainTableNames.map(::normalize).toSet()
        val rows = Csv.read(File(countryDir, config.tableFile)).map { r ->
            Row(
                r.getValue("food_code"), r.getValue("food"), r.getValue("prep_code"), r.getValue("prep"),
                NUMERIC.associateWith { r.getValue(it).takeIf(String::isNotBlank)?.toDouble() },
                r.getValue("source_code"), r.getValue("source_desc"),
            )
        }
        if (rows.isEmpty()) errors += "catalog.json: ${config.tableFile} has no rows"
        rows.forEach { row -> NUMERIC.forEach { n -> if ((row.values[n] ?: 0.0) < 0) errors += "catalog: negative $n in ${row.code}/${row.prepCode}" } }

        val unitCodes = config.units.map { it.code }
        val measures = Csv.read(File(countryDir, measuresFile)).filter { it.getValue("measure_code") in unitCodes }
            .groupBy { it.getValue("food_code") to it.getValue("prep_code") }
        fun portions(code: String, prepCodes: List<String>): List<CatalogPortion> {
            val candidates = (prepCodes + NOT_APPLICABLE).distinct().flatMap { measures[code to it].orEmpty() }
            return unitCodes.mapNotNull { unit ->
                val grams = candidates.filter { it.getValue("measure_code") == unit }.map { it.getValue("grams").toDouble() }.distinct()
                // Ambiguous (several different weights) or implausible measures are left out, not guessed.
                grams.singleOrNull()?.takeIf { it in 1.0..1000.0 }?.let { CatalogPortion(unitId(unit), it) }
            }.take(config.maxPortionsPerFood)
        }

        val mainByName = mainTable.rows.values.associateBy { normalize(it.description) }
        val aliases = mutableMapOf<String, MutableList<String>>()
        val mainPortions = mutableMapOf<String, List<CatalogPortion>>()
        val foods = mutableListOf<CatalogFood>()

        rows.groupBy { it.code }.forEach { (code, foodRows) ->
            val fromMain = foodRows.filter { it.sourceCode == config.mainTableSourceCode && normalize(it.sourceDesc) in mainByName }
            fromMain.groupBy { mainByName.getValue(normalize(it.sourceDesc)).id }.forEach { (mainId, same) ->
                // Vague names ("Pão não especificado") are not useful as everyday names.
                val name = same.first().food
                if (!normalize(name).contains("nao especificad")) aliases.getOrPut(mainId) { mutableListOf() }.let { if (name !in it) it += name }
                if (mainId !in mainPortions) portions(code, same.map { it.prepCode }).takeIf { it.isNotEmpty() }?.let { mainPortions[mainId] = it }
            }
            val own = if (normalize(foodRows.first().food) in taken) emptyList() else foodRows - fromMain.toSet()
            // Same values for several preparations -> one entry.
            own.groupBy { it.values }.values.forEach { group ->
                val v = group.first().values
                val added = v["added_sodium_mg"] ?: 0.0
                val nutrients = buildMap {
                    v["energy_kcal"]?.let { put(Nutrient.ENERGY_KCAL, it) }
                    v["carbohydrate_g"]?.let { put(Nutrient.CARBOHYDRATE_G, it) }
                    v["fiber_g"]?.let { put(Nutrient.FIBER_G, it) }
                    v["protein_g"]?.let { put(Nutrient.PROTEIN_G, it) }
                    v["lipids_g"]?.let { put(Nutrient.LIPIDS_G, it) }
                    v["sodium_mg"]?.let { put(Nutrient.SODIUM_MG, Math.round((it + added) * 100) / 100.0) }
                    v["potassium_mg"]?.let { put(Nutrient.POTASSIUM_MG, it) }
                }
                val allPreps = group.size == own.size
                val preps = group.filter { it.prepCode != NOT_APPLICABLE }.map { it.prep.lowercase() }
                val name = if (allPreps || preps.isEmpty()) group.first().food else "${group.first().food} – ${preps.joinToString(", ")}"
                foods += CatalogFood(
                    id = "${config.table}:$code:${group.first().prepCode}",
                    name = name,
                    per100g = Nutrients(nutrients),
                    portions = portions(code, group.map { it.prepCode }),
                    source = config.table,
                    addedSodiumMgPer100g = added.takeIf { it > 0 },
                    addedSugarGPer100g = v["added_sugar_g"],
                )
            }
        }
        foods.groupBy { it.id }.filter { it.value.size > 1 }.keys.forEach { errors += "catalog: duplicate id $it" }
        val units = config.units.map { HouseholdUnit(unitId(it.code), it.singular, it.plural) }
        return Result(foods, units, aliases, mainPortions)
    }

    companion object {
        private const val NOT_APPLICABLE = "99"
        private val NUMERIC = listOf("energy_kcal", "protein_g", "lipids_g", "carbohydrate_g", "fiber_g", "sugar_total_g", "added_sugar_g", "sodium_mg", "added_sodium_mg", "potassium_mg")

        fun normalize(text: String): String =
            Normalizer.normalize(text, Normalizer.Form.NFKD).replace(Regex("\\p{M}"), "").lowercase().replace(",", " ").split(" ").filter { it.isNotBlank() }.joinToString(" ")
    }
}
