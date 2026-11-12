package com.velkyss.diabem.app

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.velkyss.diabem.app.ui.theme.LightColors
import com.velkyss.diabem.app.ui.theme.contrastLights
import com.velkyss.diabem.app.ui.theme.highContrastScheme
import com.velkyss.diabem.app.ui.theme.normalLights
import com.velkyss.diabem.app.ui.theme.normalScheme
import kotlin.test.Test
import kotlin.test.assertTrue

/** Checks the colour pairs used for text against the WCAG contrast formula. */
class ContrastTest {
    private fun ratio(a: Color, b: Color): Double {
        val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (hi + 0.05) / (lo + 0.05)
    }

    private fun pairs(s: ColorScheme, l: LightColors) = mapOf(
        "onBackground/background" to (s.onBackground to s.background),
        "onSurface/surface" to (s.onSurface to s.surface),
        "onSurfaceVariant/surface" to (s.onSurfaceVariant to s.surface),
        "onSurfaceVariant/background" to (s.onSurfaceVariant to s.background),
        "onPrimary/primary" to (s.onPrimary to s.primary),
        "primary/background" to (s.primary to s.background),
        "onPrimaryContainer/primaryContainer" to (s.onPrimaryContainer to s.primaryContainer),
        "onSecondaryContainer/secondaryContainer" to (s.onSecondaryContainer to s.secondaryContainer),
        "green light" to (l.onLight to l.green),
        "amber light" to (l.onLight to l.amber),
        "red light" to (l.onLight to l.red),
        "red text/background" to (l.red to s.background),
    )

    @Test
    fun normalThemeMeetsWcagAA() {
        pairs(normalScheme, normalLights).forEach { (name, p) ->
            assertTrue(ratio(p.first, p.second) >= 4.5, "$name: ${ratio(p.first, p.second)}")
        }
    }

    @Test
    fun highContrastThemeMeetsWcagAAA() {
        pairs(highContrastScheme, contrastLights).forEach { (name, p) ->
            assertTrue(ratio(p.first, p.second) >= 7.0, "$name: ${ratio(p.first, p.second)}")
        }
    }
}
