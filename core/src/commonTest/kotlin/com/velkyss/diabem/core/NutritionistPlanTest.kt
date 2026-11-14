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

class NutritionistPlanBackupTest {
    private val plan = NutritionistPlan()
        .add(MealSlot.BREAKFAST, "1 pão francês + café com leite")
        .add(MealSlot.BREAKFAST, "1 fatia de mamão")
        .add(MealSlot.MORNING_SNACK, "1 fruta")

    @Test
    fun backupTextCanBeRestoredExactly() {
        val text = plan.toBackupText { "Refeição ${it.key}" }
        assertEquals(plan, NutritionistPlan().mergeBackupText(text))
    }

    @Test
    fun restoreAddsWithoutDeletingOrDuplicating() {
        val existing = NutritionistPlan().add(MealSlot.DINNER, "sopa").add(MealSlot.MORNING_SNACK, "1 fruta")
        val merged = existing.mergeBackupText(plan.toBackupText { it.key })
        assertEquals(listOf("sopa"), merged.optionsFor(MealSlot.DINNER))
        assertEquals(listOf("1 fruta"), merged.optionsFor(MealSlot.MORNING_SNACK))
        assertEquals(2, merged.optionsFor(MealSlot.BREAKFAST).size)
    }

    @Test
    fun backupWrittenByVersion1FormatIsStillRead() {
        // Exact text format of the first version with backups. Do not change this test: fix the code.
        val saved = """
            DiaBem – Meu plano
            [breakfast] Café da manhã
            - 1 pão francês + café com leite
            [afternoon_snack] Lanche da tarde
            - 1 iogurte natural
        """.trimIndent()
        val restored = NutritionistPlan().mergeBackupText(saved)
        assertEquals(listOf("1 pão francês + café com leite"), restored.optionsFor(MealSlot.BREAKFAST))
        assertEquals(listOf("1 iogurte natural"), restored.optionsFor(MealSlot.AFTERNOON_SNACK))
    }

    @Test
    fun nonsenseTextChangesNothing() {
        assertEquals(plan, plan.mergeBackupText("olá\n- sem refeição\n[xyz] nada\n- outra"))
    }
}

class NutritionistPlanEditTest {
    private val plan = NutritionistPlan().add(MealSlot.LUNCH, "3 colheres de arros").add(MealSlot.LUNCH, "salada")

    @Test
    fun anOptionCanBeCorrected() {
        val fixed = plan.replace(MealSlot.LUNCH, 0, "  3 colheres de arroz ")
        assertEquals(listOf("3 colheres de arroz", "salada"), fixed.optionsFor(MealSlot.LUNCH))
    }

    @Test
    fun blankDuplicateOrWrongIndexChangeNothing() {
        assertEquals(plan, plan.replace(MealSlot.LUNCH, 0, "   "))
        assertEquals(plan, plan.replace(MealSlot.LUNCH, 0, "salada"))
        assertEquals(plan, plan.replace(MealSlot.LUNCH, 5, "outra"))
        assertEquals(plan, plan.replace(MealSlot.DINNER, 0, "outra"))
    }
}

class MealRemindersTest {
    @Test
    fun defaultsAreOffAndCoverEveryMeal() {
        val r = com.velkyss.diabem.core.profile.MealReminders()
        MealSlot.entries.forEach { assertTrue(!r.reminderFor(it).enabled) }
        assertTrue(!r.anyEnabled)
        assertEquals("10:00", r.reminderFor(MealSlot.MORNING_SNACK).label)
    }

    @Test
    fun timeCanBeChangedAndWrapsAround() {
        val t = com.velkyss.diabem.core.profile.MealReminder(23, 55, enabled = true)
        assertEquals("00:55", t.plusHours(1).label)
        assertEquals("23:00", t.plusMinutes(5).label)
        assertEquals("23:50", t.plusMinutes(-5).label)
        assertEquals("07:05", com.velkyss.diabem.core.profile.MealReminder(7, 5).label)
        val r = com.velkyss.diabem.core.profile.MealReminders().with(MealSlot.MORNING_SNACK, t)
        assertTrue(r.anyEnabled)
        assertEquals(t, r.reminderFor(MealSlot.MORNING_SNACK))
    }
}
