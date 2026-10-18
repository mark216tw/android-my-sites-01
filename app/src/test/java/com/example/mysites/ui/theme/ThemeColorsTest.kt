package com.example.mysites.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.mysites.data.DEFAULT_THEME_HUE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeColorsTest {
    @Test
    fun providesSixDistinctPresetHues() {
        assertEquals(6, ThemePresets.size)
        assertEquals(6, ThemePresets.map { it.hue }.distinct().size)
        assertTrue(ThemePresets.all { it.hue in 0..360 })
    }

    @Test
    fun defaultHueMatchesAPreset() {
        assertNotNull(presetForHue(DEFAULT_THEME_HUE))
    }

    @Test
    fun lightAndDarkSchemesUseDifferentPrimaryColors() {
        ThemePresets.forEach { preset ->
            val light = colorSchemeFor(preset.hue, dark = false)
            val dark = colorSchemeFor(preset.hue, dark = true)
            assertNotEquals(light.primary, dark.primary)
        }
    }

    @Test
    fun keyTextPairsMeetContrastGuidelines() {
        val hues = ThemePresets.map { it.hue } + listOf(0, 15, 75, 135, 195, 255, 315, 360)

        hues.forEach { hue ->
            listOf(false, true).forEach { dark ->
                val scheme = colorSchemeFor(hue, dark)

                assertContrast(scheme.primary, scheme.onPrimary, hue, dark, "primary")
                assertContrast(
                    scheme.primaryContainer,
                    scheme.onPrimaryContainer,
                    hue,
                    dark,
                    "primaryContainer",
                )
                assertContrast(
                    scheme.secondaryContainer,
                    scheme.onSecondaryContainer,
                    hue,
                    dark,
                    "secondaryContainer",
                )
                assertContrast(scheme.background, scheme.onBackground, hue, dark, "background")
                assertContrast(scheme.surface, scheme.onSurface, hue, dark, "surface")
                assertContrast(
                    scheme.surfaceVariant,
                    scheme.onSurfaceVariant,
                    hue,
                    dark,
                    "surfaceVariant",
                )
            }
        }
    }

    private fun assertContrast(
        background: Color,
        foreground: Color,
        hue: Int,
        dark: Boolean,
        role: String,
    ) {
        val ratio = contrastRatio(background, foreground)
        assertTrue(
            "$role hue=$hue dark=$dark contrast=$ratio",
            ratio >= 4.5,
        )
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val firstLuminance = first.luminance()
        val secondLuminance = second.luminance()
        val lighter = maxOf(firstLuminance, secondLuminance)
        val darker = minOf(firstLuminance, secondLuminance)
        return (lighter + 0.05) / (darker + 0.05)
    }
}
