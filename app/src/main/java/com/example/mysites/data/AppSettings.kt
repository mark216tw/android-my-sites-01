package com.example.mysites.data

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class SiteSortMode {
    CUSTOM,
    NAME_ASC,
    NAME_DESC,
    LAST_USED_DESC,
    LAST_USED_ASC,
    CREATED_DESC,
    CREATED_ASC,
}

const val DEFAULT_THEME_HUE = 215

const val DEFAULT_WEB_TEXT_ZOOM = 100
const val MIN_WEB_TEXT_ZOOM = 50
const val MAX_WEB_TEXT_ZOOM = 200
const val WEB_TEXT_ZOOM_STEP = 10

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeHue: Int = DEFAULT_THEME_HUE,
    val siteSortMode: SiteSortMode = SiteSortMode.CUSTOM,
)
