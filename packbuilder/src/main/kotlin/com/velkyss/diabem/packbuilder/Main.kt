package com.velkyss.diabem.packbuilder

import com.velkyss.diabem.core.model.Nutrient
import java.io.File
import kotlin.system.exitProcess

/**
 * Usage: packbuilder <dataDir> <resourcesDir> <reportsDir>
 * Compiles every country folder in <dataDir> into <resourcesDir>/files/packs/<country>.json
 * (bundled as an app resource) and writes a readable nutrition report into <reportsDir>.
 */
fun main(args: Array<String>) {
    require(args.size == 3) { "Usage: packbuilder <dataDir> <resourcesDir> <reportsDir>" }
    val dataDir = File(args[0])
    val outDir = File(args[1], "files/packs").apply { deleteRecursively(); mkdirs() }
    val reportsDir = File(args[2]).apply { mkdirs() }
    val countries = dataDir.listFiles { f -> File(f, "pack.json").isFile }.orEmpty().sortedBy { it.name }
    var failed = false
    for (country in countries) {
        try {
            val pack = PackCompiler(country).compile()
            File(outDir, "${pack.id}.json").writeText(pack.encode())
            File(reportsDir, "nutrition-${pack.id}.md").writeText(report(pack))
            println("Pack '${pack.id}': ${pack.recipes.size} recipes, ${pack.foods.size} foods - OK")
        } catch (e: PackBuildException) {
            System.err.println("Pack '${country.name}' FAILED\n${e.message}")
            failed = true
        }
    }
    if (countries.isEmpty()) { System.err.println("No country packs found in $dataDir"); failed = true }
    if (failed) exitProcess(1)
}

private fun report(pack: com.velkyss.diabem.core.pack.CountryPack): String = buildString {
    appendLine("# Nutrition report: ${pack.id}")
    appendLine()
    appendLine("| Recipe | Meals | g/serving | kcal | Carbs g | Fiber g | Sodium mg | Added sugar g | Reviewed |")
    appendLine("|---|---|---|---|---|---|---|---|---|")
    for (r in pack.recipes) {
        fun v(n: Nutrient) = r.perServing[n]?.toString() ?: "?"
        appendLine(
            "| ${r.recipe.name} | ${r.recipe.mealTypes.joinToString { it.name.lowercase() }} | ${r.servingGrams.toInt()} | " +
                "${v(Nutrient.ENERGY_KCAL)} | ${v(Nutrient.CARBOHYDRATE_G)} | ${v(Nutrient.FIBER_G)} | ${v(Nutrient.SODIUM_MG)} | " +
                "${r.addedSugarPerServingG} | ${if (r.recipe.reviewedByNutritionist) "yes" else "no"} |",
        )
    }
}
