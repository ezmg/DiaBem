package com.velkyss.diabem.packbuilder

import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.pack.CountryPack
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PackCompilerTest {

    private val dataDir = File(System.getProperty("diabem.dataDir"))

    private fun copyOfBrazil(): File {
        val tmp = Files.createTempDirectory("pack").toFile()
        File(dataDir, "br").copyRecursively(File(tmp, "br"))
        return File(tmp, "br")
    }

    @Test
    fun brazilPackCompiles() {
        val pack = PackCompiler(File(dataDir, "br")).compile()
        assertEquals("br", pack.id)
        assertTrue(pack.recipes.size >= 30, "expected at least 30 recipes")
        MealType.entries.forEach { meal ->
            assertTrue(pack.recipes.count { meal in it.recipe.mealTypes } >= 5, "too few recipes for $meal")
        }
        pack.recipes.forEach { r ->
            assertTrue(r.perServing.isKnown(Nutrient.CARBOHYDRATE_G))
            assertTrue(r.perServing.isKnown(Nutrient.SODIUM_MG))
        }
    }

    @Test
    fun originalRecipesAreNotMarkedAsReviewed() {
        // Until a nutritionist reviews them, every recipe must say so.
        val pack = PackCompiler(File(dataDir, "br")).compile()
        assertTrue(pack.recipes.none { it.recipe.reviewedByNutritionist })
    }

    @Test
    fun nutrientsComeFromTheTacoTable() {
        val pack = PackCompiler(File(dataDir, "br")).compile()
        // TACO row 1: "Arroz, integral, cozido", carbohydrate 25.8098 g / 100 g
        val rice = pack.foods.single { it.id == "arroz-integral-cozido" }
        assertEquals(25.8098, rice.per100g[Nutrient.CARBOHYDRATE_G])
        assertEquals("taco4", rice.origin.source)
        // IBGE: arroz integral, colher de sopa = 20 g
        assertEquals(20.0, pack.measures.single { it.food == "arroz-integral-cozido" && it.unit == "colher_sopa" }.grams)
    }

    @Test
    fun packSurvivesEncodeDecode() {
        val pack = PackCompiler(File(dataDir, "br")).compile()
        assertEquals(pack, CountryPack.decode(pack.encode()))
    }

    @Test
    fun brokenReferenceFailsTheBuild() {
        val br = copyOfBrazil()
        val f = File(br, "recipes/cuscuz-com-ovo.json")
        f.writeText(f.readText().replace("\"farinha-de-milho\"", "\"farinha-inexistente\""))
        val e = assertFailsWith<PackBuildException> { PackCompiler(br).compile() }
        assertTrue(e.errors.any { "unknown food 'farinha-inexistente'" in it }, e.errors.toString())
    }

    @Test
    fun supplementCannotOverrideOfficialValue() {
        val br = copyOfBrazil()
        val f = File(br, "foods.json")
        f.writeText(f.readText().replace("\"SODIUM_MG\": {\n          \"value\": 0", "\"CARBOHYDRATE_G\": {\n          \"value\": 0"))
        val e = assertFailsWith<PackBuildException> { PackCompiler(br).compile() }
        assertTrue(e.errors.any { "supplements may only fill empty values" in it }, e.errors.toString())
    }

    @Test
    fun tacoMarkersAreParsedCorrectly() {
        assertEquals(0.0, TacoTable.parse("Tr"))
        assertEquals(0.0, TacoTable.parse("NA"))
        assertEquals(null, TacoTable.parse("*"))
        assertEquals(null, TacoTable.parse(""))
        assertEquals(12.5, TacoTable.parse("12.5"))
    }
}
