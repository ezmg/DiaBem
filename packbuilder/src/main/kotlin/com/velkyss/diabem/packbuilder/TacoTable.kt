package com.velkyss.diabem.packbuilder

import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.Nutrients
import java.io.File

/**
 * The TACO table as converted by tools/importers/taco_xlsx_to_csv.py.
 *
 * Markers used by TACO and how they are read:
 * - "Tr" (traces, below the quantification limit) -> 0
 * - "NA" (not applicable, e.g. fibre in meat)      -> 0
 * - "*", "" (not analysed)                         -> unknown (never 0)
 */
class TacoTable(rows: List<Map<String, String>>) {

    data class Row(val id: String, val description: String, val per100g: Nutrients)

    private val columns = mapOf(
        Nutrient.ENERGY_KCAL to "energy_kcal",
        Nutrient.CARBOHYDRATE_G to "carbohydrate_g",
        Nutrient.FIBER_G to "fiber_g",
        Nutrient.PROTEIN_G to "protein_g",
        Nutrient.LIPIDS_G to "lipids_g",
        Nutrient.SODIUM_MG to "sodium_mg",
        Nutrient.POTASSIUM_MG to "potassium_mg",
    )

    val rows: Map<String, Row> = rows.associate { r ->
        val id = r.getValue("taco_id")
        val values = columns.mapNotNull { (n, col) -> parse(r.getValue(col))?.let { n to it } }.toMap()
        id to Row(id, r.getValue("description"), Nutrients(values))
    }

    companion object {
        fun load(file: File) = TacoTable(Csv.read(file))

        fun parse(raw: String): Double? = when (raw.trim()) {
            "Tr", "NA" -> 0.0
            "", "*", "-" -> null
            else -> raw.trim().toDouble()
        }
    }
}

/** The IBGE household measures table as converted by tools/importers/ibge_measures_to_csv.py. */
class OfficialMeasuresTable(rows: List<Map<String, String>>) {
    data class Row(val food: String, val foodName: String, val prep: String, val measure: String, val measureName: String, val grams: Double)

    private val rows = rows.map {
        Row(it.getValue("food_code"), it.getValue("food"), it.getValue("prep_code"), it.getValue("measure_code"), it.getValue("measure"), it.getValue("grams").toDouble())
    }
    private val index = this.rows.groupBy { Triple(it.food, it.prep, it.measure) }

    fun find(ref: OfficialMeasureRef): List<Row> = index[Triple(ref.food, ref.preparation, ref.measure)].orEmpty()

    companion object {
        fun load(file: File) = OfficialMeasuresTable(Csv.read(file))
    }
}
