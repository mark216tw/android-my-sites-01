package com.example.mysites.data

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

const val DEFAULT_THEME_HUE = 215

const val DEFAULT_WEB_TEXT_ZOOM = 100
const val MIN_WEB_TEXT_ZOOM = 50
const val MAX_WEB_TEXT_ZOOM = 200
const val WEB_TEXT_ZOOM_STEP = 10

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeHue: Int = DEFAULT_THEME_HUE,
    val webTextZoom: Int = DEFAULT_WEB_TEXT_ZOOM,
)
