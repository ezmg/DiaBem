package com.velkyss.diabem.core.health

import com.velkyss.diabem.core.model.MealType
import com.velkyss.diabem.core.profile.Condition
import com.velkyss.diabem.core.profile.CustomLimits
import com.velkyss.diabem.core.profile.HealthProfile
import com.velkyss.diabem.core.search.SearchFixtures
import com.velkyss.diabem.core.search.SearchFixtures.compiled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HealthEvaluatorTest {
    private val evaluator = HealthEvaluator(SearchFixtures.criteria)
    private val diabetes = HealthProfile(setOf(Condition.DIABETES_TYPE_2))
    private val hypertension = HealthProfile(setOf(Condition.HYPERTENSION))

    @Test
    fun carbLightUsesMealThresholds() {
        assertEquals(TrafficLight.GREEN, evaluator.carbLight(compiled("chicken-rice"), MealType.LUNCH))
        assertEquals(TrafficLight.RED, evaluator.carbLight(compiled("big-rice"), MealType.LUNCH))
    }

    @Test
    fun customCarbLimitWins() {
        val strict = HealthProfile(setOf(Condition.DIABETES_TYPE_2), CustomLimits(maxCarbsPerMealG = 10.0))
        assertEquals(TrafficLight.RED, evaluator.carbLight(compiled("chicken-rice"), MealType.LUNCH, strict))
        val loose = HealthProfile(setOf(Condition.DIABETES_TYPE_2), CustomLimits(maxCarbsPerMealG = 200.0))
        assertEquals(TrafficLight.GREEN, evaluator.carbLight(compiled("big-rice"), MealType.LUNCH, loose))
    }

    @Test
    fun sodiumLightFollowsPer100gAndPerServingRules() {
        // cheese 800 mg/100 g + salt -> above 600 mg/100 g
        assertEquals(TrafficLight.RED, evaluator.sodiumLight(compiled("salty-cheese")))
        // tomato salad: almost no sodium
        assertEquals(TrafficLight.GREEN, evaluator.sodiumLight(compiled("tomato-salad")))
        // chicken-rice: 261 mg in a 210 g serving -> 124 mg/100 g -> amber
        assertEquals(TrafficLight.AMBER, evaluator.sodiumLight(compiled("chicken-rice")))
    }

    @Test
    fun relevantRedDependsOnTheCondition() {
        val salty = compiled("salty-cheese")
        assertTrue(evaluator.assess(salty, MealType.LUNCH, hypertension).relevantRed)
        assertFalse(evaluator.assess(salty, MealType.LUNCH, diabetes).relevantRed)
        val bigRice = compiled("big-rice")
        assertTrue(evaluator.assess(bigRice, MealType.LUNCH, diabetes).relevantRed)
        assertFalse(evaluator.assess(bigRice, MealType.LUNCH, hypertension).relevantRed)
    }

    @Test
    fun fiberLabels() {
        assertEquals(FiberLabel.HIGH, evaluator.fiberLabel(compiled("big-rice")))
        assertEquals(FiberLabel.NONE, evaluator.fiberLabel(compiled("tomato-salad")))
    }

    @Test
    fun profileNeedsACondition() {
        assertFailsWith<IllegalArgumentException> { HealthProfile(emptySet()) }
        assertFailsWith<IllegalArgumentException> { CustomLimits(maxCarbsPerMealG = 0.0) }
    }
}
