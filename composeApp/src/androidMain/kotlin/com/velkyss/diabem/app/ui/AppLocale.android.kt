package com.velkyss.diabem.app.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private var default: Locale? = null

    @Suppress("DEPRECATION")
    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if (default == null) default = Locale.getDefault()
        val locale = if (value == null) default!! else Locale.forLanguageTag(value)
        Locale.setDefault(locale)
        val configuration = Configuration(LocalConfiguration.current).apply { setLocale(locale) }
        val resources = LocalContext.current.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration provides configuration
    }
}
