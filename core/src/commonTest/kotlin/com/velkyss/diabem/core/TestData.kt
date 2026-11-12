package com.velkyss.diabem.core

import com.velkyss.diabem.core.model.Food
import com.velkyss.diabem.core.model.FoodMeasure
import com.velkyss.diabem.core.model.HouseholdUnit
import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.model.Nutrient
import com.velkyss.diabem.core.model.Nutrients
import com.velkyss.diabem.core.model.PantryCategory
import com.velkyss.diabem.core.model.PantryItem
import com.velkyss.diabem.core.model.Recipe
import com.velkyss.diabem.core.model.RecipeIngredient
import com.velkyss.diabem.core.model.SourceRef
import com.velkyss.diabem.core.pack.PackSources

/** Small, hand-made data set for unit tests (not real table values). */
object TestData {
    fun nutrients(kcal: Double, carbs: Double, fiber: Double?, sodium: Double) = Nutrients(
        buildMap {
            put(Nutrient.ENERGY_KCAL, kcal)
            put(Nutrient.CARBOHYDRATE_G, carbs)
            fiber?.let { put(Nutrient.FIBER_G, it) }
            put(Nutrient.PROTEIN_G, 1.0)
            put(Nutrient.LIPIDS_G, 1.0)
            put(Nutrient.SODIUM_MG, sodium)
            put(Nutrient.POTASSIUM_MG, 10.0)
        },
    )

    val rice = Food("rice", "rice", "rice", nutrients(120.0, 25.0, 2.0, 1.0), origin = SourceRef("t", "1"))
    val beans = Food("beans", "beans", "beans", nutrients(80.0, 14.0, 8.0, 2.0), origin = SourceRef("t", "2"))
    val salt = Food("salt", "salt", "salt", nutrients(0.0, 0.0, 0.0, 40000.0), origin = SourceRef("t", "3"))
    val sugar = Food("sugar", "sugar", "sugar", nutrients(390.0, 100.0, 0.0, 10.0), addedSugar = true, origin = SourceRef("t", "4"))
    val mystery = Food("mystery", "mystery", "rice", nutrients(50.0, 5.0, null, 5.0), origin = SourceRef("t", "5"))

    val foods = listOf(rice, beans, salt, sugar, mystery)
    val units = listOf(HouseholdUnit("spoon", "spoon", "spoons"), HouseholdUnit("ladle", "ladle", "ladles"))
    val measures = listOf(
        FoodMeasure("rice", "spoon", 20.0, SourceRef("m", "a")),
        FoodMeasure("beans", "ladle", 140.0, SourceRef("m", "b")),
        FoodMeasure("sugar", "spoon", 12.0, SourceRef("m", "c")),
    )

    fun recipe(
        id: String = "rice-and-beans",
        servings: Int = 2,
        ingredients: List<RecipeIngredient> = listOf(
            RecipeIngredient("rice", 6.0, "spoon"),
            RecipeIngredient("beans", 2.0, "ladle"),
            RecipeIngredient("salt", 1.0, "g"),
        ),
    ) = Recipe(id, "Rice and beans", listOf(MealType.LUNCH), servings, 20, ingredients, listOf("Cook."))

    fun sources(recipes: List<Recipe> = listOf(recipe())) = PackSources(
        categories = listOf(PantryCategory("c", "C", "icon")),
        pantry = listOf("rice", "beans", "salt", "sugar").map { PantryItem(it, it, "c") },
        foods = foods,
        units = units,
        measures = measures,
        substitutions = emptyList(),
        recipes = recipes,
    )
}
