package com.velkyss.diabem.packbuilder

import com.velkyss.diabem.core.health.HealthCriteria
import com.velkyss.diabem.core.model.Food
import com.velkyss.diabem.core.model.FoodMeasure
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.Nutrients
import com.velkyss.diabem.core.model.Recipe
import com.velkyss.diabem.core.model.SourceRef
import com.velkyss.diabem.core.nutrition.NutritionCalculator
import com.velkyss.diabem.core.pack.CountryPack
import com.velkyss.diabem.core.pack.PackSources
import com.velkyss.diabem.core.pack.PackValidator
import kotlinx.serialization.json.Json
import java.io.File

class PackBuildException(val errors: List<String>) :
    Exception("Country pack has ${errors.size} error(s):\n" + errors.joinToString("\n") { " - $it" })

/** Reads one country folder (data/<country>/), validates it and builds a [CountryPack]. */
class PackCompiler(private val countryDir: File) {

    private val json = Json { ignoreUnknownKeys = false }

    private inline fun <reified T> read(name: String): T =
        json.decodeFromString<T>(File(countryDir, name).readText(Charsets.UTF_8))

    fun compile(): CountryPack {
        val errors = mutableListOf<String>()
        val pack = read<PackFile>("pack.json")
        val pantry = read<PantryFile>("pantry.json")
        val foodsFile = read<FoodsFile>("foods.json")
        val measuresFile = read<MeasuresFile>("measures.json")
        val substitutions = read<SubstitutionsFile>("substitutions.json").substitutions
        val criteria = read<HealthCriteria>("health-criteria.json")
        val sourceIds = pack.sources.map { it.id }.toSet()

        // Foods: nutrients always come from the official table, never from hand-typed numbers.
        if (foodsFile.table !in sourceIds) errors += "foods.json: table '${foodsFile.table}' is not declared in pack.json"
        val taco = TacoTable.load(File(countryDir, foodsFile.tableFile))
        val foods = foodsFile.foods.mapNotNull { f -> buildFood(f, foodsFile, taco, sourceIds, errors) }

        // Household measures: official table first; other sources must be named.
        if (measuresFile.table !in sourceIds) errors += "measures.json: table '${measuresFile.table}' is not declared in pack.json"
        val official = OfficialMeasuresTable.load(File(countryDir, measuresFile.tableFile))
        val measures = measuresFile.measures.mapNotNull { m ->
            val where = "Measure ${m.food}/${m.unit}"
            when {
                m.official != null && m.grams != null -> { errors += "$where: use either 'official' or 'grams', not both"; null }
                m.official != null -> {
                    val found = official.find(m.official)
                    val distinct = found.map { it.grams }.distinct()
                    when {
                        found.isEmpty() -> { errors += "$where: no row ${m.official} in ${measuresFile.tableFile}"; null }
                        distinct.size > 1 -> { errors += "$where: ambiguous official row ${m.official} ($distinct)"; null }
                        else -> FoodMeasure(
                            m.food, m.unit, distinct.single(),
                            SourceRef(measuresFile.table, "${m.official.food} ${found.first().foodName} / ${m.official.measure} ${found.first().measureName}"),
                        )
                    }
                }
                m.grams != null -> {
                    if (m.source == null || m.source !in sourceIds) { errors += "$where: 'grams' needs a 'source' declared in pack.json"; null }
                    else FoodMeasure(m.food, m.unit, m.grams, SourceRef(m.source, m.reference ?: ""))
                }
                else -> { errors += "$where: needs 'official' or 'grams'"; null }
            }
        }

        val recipesDir = File(countryDir, "recipes")
        val recipes = recipesDir.listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }.mapNotNull { file ->
            runCatching { json.decodeFromString<Recipe>(file.readText(Charsets.UTF_8)) }
                .onSuccess { if (it.id != file.nameWithoutExtension) errors += "${file.name}: id '${it.id}' must match the file name" }
                .onFailure { errors += "${file.name}: ${it.message?.lineSequence()?.first()}" }
                .getOrNull()
        }
        if (recipes.isEmpty()) errors += "No recipes found in ${recipesDir.path}"

        val sources = PackSources(pantry.categories, pantry.items, foods, measuresFile.units, measures, substitutions, recipes)
        errors += PackValidator.validateSources(sources)
        criteria.allSources().filter { it !in sourceIds }.forEach { errors += "health-criteria.json: source '$it' is not declared in pack.json" }
        if (errors.isNotEmpty()) throw PackBuildException(errors)

        val calculator = NutritionCalculator(foods, measures)
        val compiled = recipes.map { calculator.compile(it) }
        compiled.forEach { errors += PackValidator.validateCompiled(it) }
        if (errors.isNotEmpty()) throw PackBuildException(errors)

        // Ship only what the recipes use.
        val usedFoods = recipes.flatMap { r -> r.ingredients.map { it.food } }.toSet()
        val keptFoods = foods.filter { it.id in usedFoods }
        val usedPantry = keptFoods.map { it.pantryItem }.toSet()
        return CountryPack(
            id = pack.id,
            locale = pack.locale,
            sources = pack.sources,
            categories = pantry.categories.filter { c -> pantry.items.any { it.category == c.id && it.id in usedPantry } },
            pantry = pantry.items.filter { it.id in usedPantry },
            foods = keptFoods,
            units = measuresFile.units,
            measures = measures.filter { it.food in usedFoods },
            substitutions = substitutions.filter { it.from in usedPantry && it.to in usedPantry },
            healthCriteria = criteria,
            recipes = compiled,
        )
    }
}

private fun buildFood(
    f: FoodEntry,
    file: FoodsFile,
    taco: TacoTable,
    sourceIds: Set<String>,
    errors: MutableList<String>,
): Food? {
    val where = "Food '${f.id}'"
    if (f.nutrientFree) {
        if (f.tableId != null || f.supplements.isNotEmpty()) { errors += "$where: a nutrient-free food cannot have a table row or supplements"; return null }
        val src = f.nutrientFreeSource
        if (src == null || src !in sourceIds) { errors += "$where: 'nutrientFreeSource' must be declared in pack.json"; return null }
        return Food(f.id, f.name, f.pantryItem, Nutrients.ZERO, f.addedSugar, SourceRef(src, f.name))
    }
    val tableId = f.tableId ?: run { errors += "$where: missing 'tableId'"; return null }
    val row = taco.rows[tableId] ?: run { errors += "$where: row $tableId not found in ${file.tableFile}"; return null }
    val values = row.per100g.values.toMutableMap()
    val supplements = mutableMapOf<Nutrient, SourceRef>()
    f.supplements.forEach { (nutrient, sup) ->
        when {
            nutrient in values -> errors += "$where: $nutrient exists in the official table; supplements may only fill empty values"
            sup.source !in sourceIds -> errors += "$where: supplement source '${sup.source}' is not declared in pack.json"
            sup.value < 0 -> errors += "$where: negative supplement for $nutrient"
            else -> { values[nutrient] = sup.value; supplements[nutrient] = SourceRef(sup.source, sup.reference) }
        }
    }
    return Food(f.id, f.name, f.pantryItem, Nutrients(values), f.addedSugar, SourceRef(file.table, "$tableId - ${row.description}"), supplements)
}

private fun HealthCriteria.allSources(): Set<String> =
    (carbohydrate.sources + sodium.sources + fiber.sources + addedSugar.sources).toSet()
