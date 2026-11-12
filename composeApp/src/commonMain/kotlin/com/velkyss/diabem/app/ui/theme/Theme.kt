package com.velkyss.diabem.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Traffic-light colours. Always shown together with a word and an icon, never colour alone. */
@Immutable
data class LightColors(val green: Color, val amber: Color, val red: Color, val onLight: Color)

val LocalLightColors = staticCompositionLocalOf { normalLights }

internal val normalLights = LightColors(Color(0xFF1B5E20), Color(0xFF8A4B00), Color(0xFFB71C1C), Color.White)
internal val contrastLights = LightColors(Color(0xFF003300), Color(0xFF5C2E00), Color(0xFF7F0000), Color.White)

// All text/background pairs below have a contrast ratio of at least 7:1 (WCAG AAA) in high contrast,
// and at least 4.5:1 (WCAG AA) in the normal scheme.
internal val normalScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF0B5D4B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBE2),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF3F5B54),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0ECE8),
    onSecondaryContainer = Color(0xFF0F1F1B),
    background = Color(0xFFFAFAF7),
    onBackground = Color(0xFF151816),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF151816),
    surfaceVariant = Color(0xFFEDF1EE),
    onSurfaceVariant = Color(0xFF34403B),
    outline = Color(0xFF5E6B66),
    error = Color(0xFFB3261E),
)

internal val highContrastScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF00382D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFFFFF),
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF59D),
    onSecondaryContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color.White,
    onSurfaceVariant = Color.Black,
    outline = Color.Black,
    error = Color(0xFF7F0000),
)

/** Large type by default: older adults are the main audience. */
private val bigTypography = Typography(
    headlineMedium = TextStyle(fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 30.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 27.sp),
    labelLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
)

/** Minimum touch target for every button and row (larger than the 48 dp guideline). */
val MinTouch = 56.dp

@Composable
fun DiaBemTheme(highContrast: Boolean, textScale: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * textScale),
        LocalLightColors provides if (highContrast) contrastLights else normalLights,
    ) {
        MaterialTheme(
            colorScheme = if (highContrast) highContrastScheme else normalScheme,
            typography = bigTypography,
            content = content,
        )
    }
}
