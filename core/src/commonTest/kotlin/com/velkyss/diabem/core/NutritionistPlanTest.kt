package com.velkyss.diabem.core

import com.velkyss.diabem.core.model.MealSlot
import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.profile.NutritionistPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionistPlanTest {
    @Test
    fun addsAndRemovesOptionsPerMeal() {
        val plan = NutritionistPlan()
            .add(MealSlot.BREAKFAST, "1 pão francês + café com leite")
            .add(MealSlot.BREAKFAST, "1 fatia de mamão")
            .add(MealSlot.MORNING_SNACK, "1 fruta")
        assertEquals(listOf("1 pão francês + café com leite", "1 fatia de mamão"), plan.optionsFor(MealSlot.BREAKFAST))
        assertEquals(listOf("1 fruta"), plan.optionsFor(MealSlot.MORNING_SNACK))
        assertEquals(emptyList(), plan.optionsFor(MealSlot.AFTERNOON_SNACK))
        assertEquals(listOf("1 fatia de mamão"), plan.remove(MealSlot.BREAKFAST, 0).optionsFor(MealSlot.BREAKFAST))
        assertEquals(plan, plan.remove(MealSlot.LUNCH, 3))
    }

    @Test
    fun textIsKeptAsWrittenExceptForExtraSpaces() {
        val plan = NutritionistPlan().add(MealSlot.LUNCH, "  3 colheres de arroz   +  salada à vontade ")
        assertEquals(listOf("3 colheres de arroz + salada à vontade"), plan.optionsFor(MealSlot.LUNCH))
    }

    @Test
    fun blankRepeatedAndTooManyOptionsAreIgnored() {
        var plan = NutritionistPlan().add(MealSlot.DINNER, "   ").add(MealSlot.DINNER, "sopa").add(MealSlot.DINNER, "sopa")
        assertEquals(listOf("sopa"), plan.optionsFor(MealSlot.DINNER))
        repeat(20) { plan = plan.add(MealSlot.DINNER, "opção $it") }
        assertEquals(NutritionistPlan.MAX_OPTIONS_PER_SLOT, plan.optionsFor(MealSlot.DINNER).size)
        assertTrue(NutritionistPlan().isEmpty)
    }

    @Test
    fun bothSnackSlotsUseSnackRecipes() {
        assertEquals(MealType.SNACK, MealSlot.MORNING_SNACK.mealType)
        assertEquals(MealType.SNACK, MealSlot.AFTERNOON_SNACK.mealType)
        assertEquals(5, MealSlot.entries.size)
    }
}
