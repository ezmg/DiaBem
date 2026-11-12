package com.velkyss.diabem.core

import kotlin.test.Test
import kotlin.test.assertEquals

class AppInfoTest {
    @Test
    fun defaultCountryIsBrazil() {
        assertEquals("br", AppInfo.DEFAULT_COUNTRY)
    }
}
