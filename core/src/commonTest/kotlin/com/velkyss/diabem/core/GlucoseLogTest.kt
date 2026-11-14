package com.velkyss.diabem.core

import com.velkyss.diabem.core.profile.GlucoseLog
import com.velkyss.diabem.core.profile.GlucoseMoment
import com.velkyss.diabem.core.profile.LocalDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalDayTest {
    @Test
    fun epochDayRoundTrip() {
        assertEquals(0, LocalDay(1970, 1, 1).toEpochDay())
        assertEquals(LocalDay(2026, 11, 14), LocalDay.fromEpochDay(LocalDay(2026, 11, 14).toEpochDay()))
        assertEquals(20771, LocalDay(2026, 11, 14).toEpochDay())
    }

    @Test
    fun daysMoveAcrossMonthsYearsAndLeapDays() {
        assertEquals(LocalDay(2026, 12, 1), LocalDay(2026, 11, 30).plusDays(1))
        assertEquals(LocalDay(2026, 12, 31), LocalDay(2027, 1, 1).plusDays(-1))
        assertEquals(LocalDay(2028, 2, 29), LocalDay(2028, 2, 28).plusDays(1))
        assertEquals(LocalDay(2027, 3, 1), LocalDay(2027, 2, 28).plusDays(1))
    }

    @Test
    fun textForms() {
        val d = LocalDay(2026, 3, 5)
        assertEquals("2026-03-05", d.iso)
        assertEquals("05/03/2026", d.display)
        assertEquals("05/03", d.shortDisplay)
        assertEquals(d, LocalDay.parse("2026-03-05"))
        assertNull(LocalDay.parse("2026-02-30"))
        assertNull(LocalDay.parse("ontem"))
    }
}

class GlucoseLogTest {
    private val today = LocalDay(2026, 11, 14)
    private val log = GlucoseLog()
        .with(today, GlucoseMoment.FASTING, 98)
        .with(today, GlucoseMoment.AFTER_LUNCH, 142)
        .with(today.plusDays(-1), GlucoseMoment.AFTER_BREAKFAST, 130)

    @Test
    fun valuesAreKeptByDayAndMoment() {
        assertEquals(mapOf(GlucoseMoment.FASTING to 98, GlucoseMoment.AFTER_LUNCH to 142), log.valuesFor(today))
        assertEquals(listOf(today, today.plusDays(-1)), log.days)
        assertEquals(emptyMap(), log.valuesFor(today.plusDays(3)))
    }

    @Test
    fun valuesCanBeCorrectedAndRemoved() {
        assertEquals(101, log.with(today, GlucoseMoment.FASTING, 101).valuesFor(today)[GlucoseMoment.FASTING])
        val removed = log.with(today.plusDays(-1), GlucoseMoment.AFTER_BREAKFAST, null)
        assertEquals(listOf(today), removed.days)
        assertTrue(GlucoseLog().isEmpty)
    }

    @Test
    fun impossibleNumbersAreIgnoredAsTypos() {
        assertEquals(log, log.with(today, GlucoseMoment.AFTER_DINNER, 5))
        assertEquals(log, log.with(today, GlucoseMoment.AFTER_DINNER, 6000))
    }

    @Test
    fun shareTextListsEveryDayNewestFirstWithoutAnyJudgement() {
        val text = log.toShareText("Minhas medições") { it.name.lowercase() }
        assertEquals(
            """
            Minhas medições
            14/11/2026
            - fasting: 98 mg/dL
            - after_lunch: 142 mg/dL
            13/11/2026
            - after_breakfast: 130 mg/dL
            """.trimIndent(),
            text,
        )
    }
}
