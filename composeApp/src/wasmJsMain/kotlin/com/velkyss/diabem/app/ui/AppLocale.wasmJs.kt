package com.velkyss.diabem.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

/** Read by the small script in index.html that overrides navigator.languages. */
private fun setCustomLocale(tag: String?): Unit = js("{ window.__customLocale = tag; }")

actual object LocalAppLocale {
    private val LocalLocale = staticCompositionLocalOf<String?> { null }

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        setCustomLocale(value)
        return LocalLocale provides value
    }
}
