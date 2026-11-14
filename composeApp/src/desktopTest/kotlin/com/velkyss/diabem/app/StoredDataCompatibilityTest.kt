package com.velkyss.diabem.app

import com.russhwolf.settings.MapSettings
import com.velkyss.diabem.app.data.UserStore
import com.velkyss.diabem.core.model.MealSlot
import com.velkyss.diabem.core.profile.Condition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * People already have the app installed with their data. An update must never lose it.
 * These are the exact keys and values written by version 1.0.0.
 * DO NOT edit the stored values to make a test pass: fix the code (or write a migration).
 */
class StoredDataCompatibilityTest {
    private val storedByV1 = mapOf<String, Any>(
        "notice_accepted_v1" to true,
        "health_profile_v1" to """{"conditions":["diabetes_type_2","hypertension"],"customLimits":{"maxCarbsPerMealG":40.0,"maxRiceSpoonsPerMeal":3}}""",
        "nutritionist_plan_v1" to """{"options":{"breakfast":["1 pão francês com queijo branco + café com leite sem açúcar"],"morning_snack":["1 fruta"],"dinner":["sopa de legumes","omelete com salada"]}}""",
        "favorites_v1" to "arroz-feijao-e-ovo,cafe-com-leite",
        "display_prefs_v1" to """{"textScale":1.3,"highContrast":true}""",
    )

    private val store = UserStore(MapSettings(storedByV1.toMutableMap()))

    @Test
    fun nutritionistPlanSurvivesTheUpdate() {
        val plan = store.plan
        assertEquals(listOf("1 pão francês com queijo branco + café com leite sem açúcar"), plan.optionsFor(MealSlot.BREAKFAST))
        assertEquals(listOf("1 fruta"), plan.optionsFor(MealSlot.MORNING_SNACK))
        assertEquals(listOf("sopa de legumes", "omelete com salada"), plan.optionsFor(MealSlot.DINNER))
    }

    @Test
    fun profileFavoritesAndPreferencesSurviveTheUpdate() {
        assertTrue(store.noticeAccepted)
        val profile = store.profile!!
        assertEquals(setOf(Condition.DIABETES_TYPE_2, Condition.HYPERTENSION), profile.conditions)
        assertEquals(40.0, profile.customLimits.maxCarbsPerMealG)
        assertEquals(3, profile.customLimits.maxRiceSpoonsPerMeal)
        assertEquals(setOf("arroz-feijao-e-ovo", "cafe-com-leite"), store.favorites)
        assertEquals(1.3f, store.displayPrefs.textScale)
        assertTrue(store.displayPrefs.highContrast)
    }

    @Test
    fun remindersStoredByVersion1Dot1SurviveLaterUpdates() {
        // Exact value written by the first version with reminders. Do not edit: fix the code.
        val later = UserStore(MapSettings(mutableMapOf<String, Any>("meal_reminders_v1" to """{"times":{"morning_snack":{"hour":10,"minute":30,"enabled":true}}}""")))
        val reminder = later.reminders.reminderFor(MealSlot.MORNING_SNACK)
        assertEquals("10:30", reminder.label)
        assertTrue(reminder.enabled)
        // data from 1.0.0 has no reminders: they start switched off
        assertTrue(!store.reminders.anyEnabled)
    }
}
