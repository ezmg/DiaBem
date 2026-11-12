package com.velkyss.diabem.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

actual object LocalAppLocale {
    private var default: Locale? = null
    private val LocalLocale = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if (default == null) default = Locale.getDefault()
        val locale = if (value == null) default!! else Locale.forLanguageTag(value)
        Locale.setDefault(locale)
        return LocalLocale provides locale.toLanguageTag()
    }
}
