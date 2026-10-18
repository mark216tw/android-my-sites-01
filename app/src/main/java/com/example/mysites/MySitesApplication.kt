package com.example.mysites

import android.app.Application
import com.example.mysites.data.SettingsStore
import com.example.mysites.data.SiteStore

class MySitesApplication : Application() {
    val siteStore: SiteStore by lazy { SiteStore(this) }
    val settingsStore: SettingsStore by lazy { SettingsStore(this) }
}
