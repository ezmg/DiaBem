package com.velkyss.diabem.core.search

import com.velkyss.diabem.core.health.AddedSugarCriterion
import com.velkyss.diabem.core.health.CarbCriterion
import com.velkyss.diabem.core.health.CriterionStatus
import com.velkyss.diabem.core.health.FiberCriterion
import com.velkyss.diabem.core.health.HealthCriteria
import com.velkyss.diabem.core.health.SodiumCriterion
import com.velkyss.diabem.core.health.TrafficThresholds
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
import com.velkyss.diabem.core.model.Substitution
import com.velkyss.diabem.core.nutrition.NutritionCalculator
import com.velkyss.diabem.core.pack.CountryPack

/** A tiny fake country with made-up foods, for logic tests only. */
object SearchFixtures {
    private fun food(id: String, pantry: String, carbs: Double, sodium: Double, fiber: Double = 1.0) = Food(
        id, id, pantry,
        Nutrients(mapOf(Nutrient.ENERGY_KCAL to 100.0, Nutrient.CARBOHYDRATE_G to carbs, Nutrient.FIBER_G to fiber, Nutrient.SODIUM_MG to sodium)),
        origin = SourceRef("t", id),
    )

    val foods = listOf(
        food("rice", "rice", 25.0, 1.0),
        food("beans", "beans", 14.0, 2.0, fiber = 8.0),
        food("chicken", "chicken", 0.0, 60.0),
        food("fish", "fish", 0.0, 70.0),
        food("tomato", "tomato", 3.0, 1.0),
        food("onion", "onion", 9.0, 1.0),
        food("cheese", "cheese", 3.0, 800.0),
        food("salt", "salt", 0.0, 40000.0),
        food("water", "water", 0.0, 0.0),
        food("chives", "chives", 3.0, 2.0),
    )

    private val measures = listOf(
        FoodMeasure("rice", "colher_sopa", 20.0, SourceRef("m", "1")),
        FoodMeasure("beans", "concha", 140.0, SourceRef("m", "2")),
        FoodMeasure("tomato", "unidade", 100.0, SourceRef("m", "3")),
        FoodMeasure("onion", "unidade", 70.0, SourceRef("m", "4")),
    )

    private fun ing(food: String, q: Double, unit: String, optional: Boolean = false) = RecipeIngredient(food, q, unit, optional = optional)

    val recipes = listOf(
        // ~31 g carbs per serving, low sodium
        Recipe("chicken-rice", "Chicken with rice", listOf(MealType.LUNCH, MealType.DINNER), 2, 30,
            listOf(ing("chicken", 200.0, "g"), ing("rice", 6.0, "colher_sopa"), ing("tomato", 1.0, "unidade"), ing("salt", 1.0, "g"), ing("chives", 5.0, "g", optional = true)),
            listOf("Cook.")),
        // fish version: same shape
        Recipe("fish-tomato", "Fish with tomato", listOf(MealType.LUNCH), 2, 30,
            listOf(ing("fish", 200.0, "g"), ing("tomato", 2.0, "unidade"), ing("onion", 1.0, "unidade"), ing("salt", 1.0, "g")),
            listOf("Cook.")),
        // very high carbs: 12 spoons of rice + beans per serving
        Recipe("big-rice", "Big rice plate", listOf(MealType.LUNCH), 1, 20,
            listOf(ing("rice", 12.0, "colher_sopa"), ing("beans", 2.0, "concha")),
            listOf("Cook.")),
        // very salty
        Recipe("salty-cheese", "Salty cheese", listOf(MealType.LUNCH, MealType.SNACK), 1, 5,
            listOf(ing("cheese", 100.0, "g"), ing("tomato", 1.0, "unidade"), ing("salt", 1.0, "g")),
            listOf("Serve.")),
        Recipe("tomato-salad", "Tomato salad", listOf(MealType.SNACK), 1, 5,
            listOf(ing("tomato", 1.0, "unidade"), ing("onion", 0.5, "unidade")),
            listOf("Mix.")),
    )

    val criteria = HealthCriteria(
        carbohydrate = CarbCriterion(
            MealType.entries.associateWith { TrafficThresholds(45.0, 60.0) }, CriterionStatus.PENDING_REVIEW, listOf("x"), "",
        ),
        sodium = SodiumCriterion(2000.0, 120.0, 600.0, 0.3, CriterionStatus.DERIVED, listOf("x"), ""),
        fiber = FiberCriterion(25.0, 14.0, 2.5, 5.0, CriterionStatus.PENDING_REVIEW, listOf("x"), ""),
        addedSugar = AddedSugarCriterion(50.0, CriterionStatus.OFFICIAL, listOf("x"), ""),
    )

    val pack: CountryPack by lazy {
        val calc = NutritionCalculator(foods, measures)
        CountryPack(
            id = "test",
            locale = "pt-BR",
            sources = emptyList(),
            categories = listOf(PantryCategory("c", "C", "i")),
            pantry = foods.map { PantryItem(it.pantryItem, it.pantryItem, "c", staple = it.id in setOf("salt", "water")) },
            foods = foods,
            units = listOf(HouseholdUnit("colher_sopa", "colher", "colheres")),
            measures = measures,
            substitutions = listOf(Substitution("chicken", "fish"), Substitution("fish", "chicken")),
            healthCriteria = criteria,
            recipes = recipes.map { calc.compile(it) },
            ricePantryItems = setOf("rice"),
        )
    }

    fun compiled(id: String) = pack.recipes.single { it.id == id }
}
