package com.example.mysites.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.mysites.R

data class ThemePreset(
    val hue: Int,
    @param:StringRes val nameRes: Int,
)

val ThemePresets = listOf(
    ThemePreset(hue = 8, nameRes = R.string.theme_preset_coral),
    ThemePreset(hue = 40, nameRes = R.string.theme_preset_mango),
    ThemePreset(hue = 95, nameRes = R.string.theme_preset_lime),
    ThemePreset(hue = 165, nameRes = R.string.theme_preset_mint),
    ThemePreset(hue = 215, nameRes = R.string.theme_preset_sky),
    ThemePreset(hue = 285, nameRes = R.string.theme_preset_grape),
)

fun presetForHue(hue: Int): ThemePreset? {
    val normalized = normalizeHue(hue)
    return ThemePresets.firstOrNull { it.hue == normalized }
}

fun themeSeedColor(hue: Int): Color {
    return Color.hsl(normalizeHue(hue).toFloat(), 0.72f, 0.55f)
}

fun colorSchemeFor(hue: Int, dark: Boolean): ColorScheme {
    val normalizedHue = normalizeHue(hue).toFloat()
    return if (dark) darkScheme(normalizedHue) else lightScheme(normalizedHue)
}

private fun normalizeHue(hue: Int): Int = ((hue % 360) + 360) % 360

private fun hsl(hue: Float, saturation: Float, lightness: Float): Color {
    return Color.hsl(hue, saturation, lightness)
}

private fun onColor(background: Color): Color {
    return if (background.luminance() > 0.179f) Color(0xFF1A1C1E) else Color.White
}

private fun lightScheme(hue: Float): ColorScheme {
    val secondaryHue = (hue + 24f) % 360f
    val tertiaryHue = (hue + 180f) % 360f
    val primary = hsl(hue, 0.68f, 0.42f)
    val secondary = hsl(secondaryHue, 0.52f, 0.40f)
    val tertiary = hsl(tertiaryHue, 0.48f, 0.42f)

    return lightColorScheme(
        primary = primary,
        onPrimary = onColor(primary),
        primaryContainer = hsl(hue, 0.75f, 0.90f),
        onPrimaryContainer = hsl(hue, 0.70f, 0.18f),
        inversePrimary = hsl(hue, 0.60f, 0.76f),
        secondary = secondary,
        onSecondary = onColor(secondary),
        secondaryContainer = hsl(secondaryHue, 0.55f, 0.90f),
        onSecondaryContainer = hsl(secondaryHue, 0.55f, 0.18f),
        tertiary = tertiary,
        onTertiary = onColor(tertiary),
        tertiaryContainer = hsl(tertiaryHue, 0.55f, 0.90f),
        onTertiaryContainer = hsl(tertiaryHue, 0.55f, 0.18f),
        background = hsl(hue, 0.25f, 0.985f),
        onBackground = hsl(hue, 0.22f, 0.12f),
        surface = hsl(hue, 0.24f, 0.975f),
        onSurface = hsl(hue, 0.22f, 0.12f),
        surfaceVariant = hsl(hue, 0.20f, 0.91f),
        onSurfaceVariant = hsl(hue, 0.16f, 0.36f),
        surfaceTint = primary,
        inverseSurface = hsl(hue, 0.15f, 0.20f),
        inverseOnSurface = hsl(hue, 0.10f, 0.95f),
        outline = hsl(hue, 0.10f, 0.50f),
        outlineVariant = hsl(hue, 0.12f, 0.80f),
        surfaceBright = hsl(hue, 0.24f, 0.99f),
        surfaceDim = hsl(hue, 0.18f, 0.86f),
        surfaceContainerLowest = hsl(hue, 0.20f, 1.0f),
        surfaceContainerLow = hsl(hue, 0.22f, 0.965f),
        surfaceContainer = hsl(hue, 0.20f, 0.94f),
        surfaceContainerHigh = hsl(hue, 0.18f, 0.915f),
        surfaceContainerHighest = hsl(hue, 0.16f, 0.89f),
    )
}

private fun darkScheme(hue: Float): ColorScheme {
    val secondaryHue = (hue + 24f) % 360f
    val tertiaryHue = (hue + 180f) % 360f
    val primary = hsl(hue, 0.62f, 0.76f)

    return darkColorScheme(
        primary = primary,
        onPrimary = hsl(hue, 0.70f, 0.16f),
        primaryContainer = hsl(hue, 0.55f, 0.30f),
        onPrimaryContainer = hsl(hue, 0.70f, 0.93f),
        inversePrimary = hsl(hue, 0.65f, 0.45f),
        secondary = hsl(secondaryHue, 0.48f, 0.76f),
        onSecondary = hsl(secondaryHue, 0.55f, 0.15f),
        secondaryContainer = hsl(secondaryHue, 0.40f, 0.28f),
        onSecondaryContainer = hsl(secondaryHue, 0.55f, 0.93f),
        tertiary = hsl(tertiaryHue, 0.46f, 0.76f),
        onTertiary = hsl(tertiaryHue, 0.55f, 0.15f),
        tertiaryContainer = hsl(tertiaryHue, 0.40f, 0.28f),
        onTertiaryContainer = hsl(tertiaryHue, 0.55f, 0.93f),
        background = hsl(hue, 0.18f, 0.075f),
        onBackground = hsl(hue, 0.10f, 0.92f),
        surface = hsl(hue, 0.16f, 0.095f),
        onSurface = hsl(hue, 0.08f, 0.93f),
        surfaceVariant = hsl(hue, 0.14f, 0.28f),
        onSurfaceVariant = hsl(hue, 0.12f, 0.78f),
        surfaceTint = primary,
        inverseSurface = hsl(hue, 0.10f, 0.92f),
        inverseOnSurface = hsl(hue, 0.15f, 0.18f),
        outline = hsl(hue, 0.10f, 0.58f),
        outlineVariant = hsl(hue, 0.12f, 0.32f),
        surfaceBright = hsl(hue, 0.12f, 0.24f),
        surfaceDim = hsl(hue, 0.14f, 0.07f),
        surfaceContainerLowest = hsl(hue, 0.15f, 0.06f),
        surfaceContainerLow = hsl(hue, 0.15f, 0.11f),
        surfaceContainer = hsl(hue, 0.14f, 0.14f),
        surfaceContainerHigh = hsl(hue, 0.13f, 0.17f),
        surfaceContainerHighest = hsl(hue, 0.12f, 0.21f),
    )
}
