package com.velkyss.diabem.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key

/**
 * Lets the app choose the language of its texts instead of following the device.
 * Recipes and ingredients come from the country pack in one language, so the rest of the
 * interface must use the same language: a half-translated screen confuses people.
 */
expect object LocalAppLocale {
    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

/** Shows [content] with every text in [languageTag] (for example "pt-BR"). */
@Composable
fun WithAppLanguage(languageTag: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLocale provides languageTag) {
        key(languageTag) { content() }
    }
}
