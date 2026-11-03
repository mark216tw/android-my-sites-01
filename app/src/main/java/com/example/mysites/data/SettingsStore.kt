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

    fun lastWebUrl(siteId: String): String? {
        val url = prefs.getString(KEY_WEB_URL_PREFIX + siteId, null)
            ?.takeIf { it.isNotBlank() }
        val savedAt = prefs.getLong(KEY_WEB_URL_TIME_PREFIX + siteId, 0L)
        val now = System.currentTimeMillis()
        val valid = url != null &&
            savedAt > 0L &&
            now >= savedAt &&
            now - savedAt <= WEB_URL_RESTORE_WINDOW_MS
        if (!valid) {
            if (url != null || savedAt != 0L) {
                clearLastWebUrl(siteId)
            }
            return null
        }
        return url
    }

    fun setLastWebUrl(siteId: String, url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return
        prefs.edit {
            putString(KEY_WEB_URL_PREFIX + siteId, trimmed)
            putLong(KEY_WEB_URL_TIME_PREFIX + siteId, System.currentTimeMillis())
        }
    }

    fun clearLastWebUrl(siteId: String) {
        prefs.edit {
            remove(KEY_WEB_URL_PREFIX + siteId)
            remove(KEY_WEB_URL_TIME_PREFIX + siteId)
        }
    }

    fun clearAllLastWebUrls() {
        val keys = prefs.all.keys.filter {
            it.startsWith(KEY_WEB_URL_PREFIX) || it.startsWith(KEY_WEB_URL_TIME_PREFIX)
        }
        if (keys.isEmpty()) return
        prefs.edit {
            keys.forEach { key -> remove(key) }
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
        const val KEY_WEB_URL_PREFIX = "web_url_"
        const val KEY_WEB_URL_TIME_PREFIX = "web_url_time_"
    }
}
