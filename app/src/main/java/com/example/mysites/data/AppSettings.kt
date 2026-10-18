package com.example.mysites.data

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

const val DEFAULT_THEME_HUE = 215

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeHue: Int = DEFAULT_THEME_HUE,
)
