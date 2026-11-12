package com.velkyss.diabem.core.search

import com.velkyss.diabem.core.search.SearchFixtures.compiled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ServingScalerTest {

    @Test
    fun doublingServingsDoublesAmounts() {
        val scaled = ServingScaler.scale(compiled("chicken-rice"), 4)
        assertEquals(400.0, scaled[0].quantity) // chicken g
        assertEquals(12.0, scaled[1].quantity) // rice spoons
        assertEquals(240.0, scaled[1].grams)
        assertEquals(2.0, scaled[3].quantity) // salt g
    }

    @Test
    fun halvingRoundsToKitchenFractions() {
        val scaled = ServingScaler.scale(compiled("fish-tomato"), 1)
        assertEquals(1.0, scaled[1].quantity) // 2 tomatoes -> 1
        assertEquals(0.5, scaled[2].quantity) // 1 onion -> 1/2
        assertEquals(0.5, scaled[3].quantity) // 1 g salt -> 0.5 g
    }

    @Test
    fun householdUnitsNeverRoundToZero() {
        assertEquals(0.25, ServingScaler.roundForKitchen(0.05, "colher_sopa"))
        assertEquals(0.75, ServingScaler.roundForKitchen(0.7, "unidade"))
        assertEquals(0.1, ServingScaler.roundForKitchen(0.01, "g"))
        assertEquals(255.0, ServingScaler.roundForKitchen(253.0, "g"))
        assertEquals(33.0, ServingScaler.roundForKitchen(33.3, "ml"))
    }

    @Test
    fun servingsOutOfRangeFail() {
        assertFailsWith<IllegalArgumentException> { ServingScaler.scale(compiled("fish-tomato"), 0) }
        assertFailsWith<IllegalArgumentException> { ServingScaler.scale(compiled("fish-tomato"), 13) }
    }

    @Test
    fun quantitiesAreFormattedLikePeopleSayThem() {
        assertEquals("1/2", QuantityFormatter.format(0.5))
        assertEquals("1 1/4", QuantityFormatter.format(1.25))
        assertEquals("3", QuantityFormatter.format(3.0))
        assertEquals("3/4", QuantityFormatter.format(0.75))
        assertEquals("0,3", QuantityFormatter.format(0.3))
        assertEquals("1.4", QuantityFormatter.format(1.4, '.'))
    }
}
