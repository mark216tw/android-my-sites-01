package com.example.mysites.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _settings.update { it.copy(themeMode = mode) }
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
    }

    fun setThemeHue(hue: Int, persist: Boolean = true) {
        val safeHue = hue.coerceIn(0, 360)
        _settings.update { it.copy(themeHue = safeHue) }
        if (persist) {
            prefs.edit { putInt(KEY_THEME_HUE, safeHue) }
        }
    }

    fun persistThemeHue() {
        prefs.edit { putInt(KEY_THEME_HUE, _settings.value.themeHue) }
    }

    fun setWebTextZoom(zoom: Int) {
        val safeZoom = zoom.coerceIn(MIN_WEB_TEXT_ZOOM, MAX_WEB_TEXT_ZOOM)
        _settings.update { it.copy(webTextZoom = safeZoom) }
        prefs.edit { putInt(KEY_WEB_TEXT_ZOOM, safeZoom) }
    }

    private fun readSettings(): AppSettings {
        val storedMode = prefs.getString(KEY_THEME_MODE, null)
        val mode = storedMode
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.SYSTEM
        val hue = prefs.getInt(KEY_THEME_HUE, DEFAULT_THEME_HUE).coerceIn(0, 360)
        val zoom = prefs.getInt(KEY_WEB_TEXT_ZOOM, DEFAULT_WEB_TEXT_ZOOM)
            .coerceIn(MIN_WEB_TEXT_ZOOM, MAX_WEB_TEXT_ZOOM)
        return AppSettings(themeMode = mode, themeHue = hue, webTextZoom = zoom)
    }

    private companion object {
        const val PREFS_NAME = "settings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_THEME_HUE = "theme_hue"
        const val KEY_WEB_TEXT_ZOOM = "web_text_zoom"
    }
}
