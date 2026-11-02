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

    fun setSiteSortMode(mode: SiteSortMode) {
        _settings.update { it.copy(siteSortMode = mode) }
        prefs.edit { putString(KEY_SITE_SORT_MODE, mode.name) }
    }

    fun lastSiteId(): String? = prefs.getString(KEY_LAST_SITE_ID, null)
        ?.takeIf { it.isNotBlank() }

    fun setLastSiteId(id: String?) {
        prefs.edit {
            if (id == null) {
                remove(KEY_LAST_SITE_ID)
            } else {
                putString(KEY_LAST_SITE_ID, id)
            }
        }
    }

    private fun readSettings(): AppSettings {
        val storedMode = prefs.getString(KEY_THEME_MODE, null)
        val mode = storedMode
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.SYSTEM
        val hue = prefs.getInt(KEY_THEME_HUE, DEFAULT_THEME_HUE).coerceIn(0, 360)
        val storedSortMode = prefs.getString(KEY_SITE_SORT_MODE, null)
        val sortMode = storedSortMode
            ?.let { name -> SiteSortMode.entries.firstOrNull { it.name == name } }
            ?: SiteSortMode.CUSTOM
        return AppSettings(themeMode = mode, themeHue = hue, siteSortMode = sortMode)
    }

    private companion object {
        const val PREFS_NAME = "settings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_THEME_HUE = "theme_hue"
        const val KEY_SITE_SORT_MODE = "site_sort_mode"
        const val KEY_LAST_SITE_ID = "last_site_id"
    }
}
