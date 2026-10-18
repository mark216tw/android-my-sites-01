package com.example.mysites

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mysites.ui.MySitesApp
import com.example.mysites.ui.theme.MySitesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MySitesApplication
        setContent {
            val settings by app.settingsStore.settings.collectAsStateWithLifecycle()

            MySitesTheme(
                themeMode = settings.themeMode,
                themeHue = settings.themeHue,
            ) {
                MySitesApp(
                    store = app.siteStore,
                    settingsStore = app.settingsStore,
                )
            }
        }
    }

    override fun onStop() {
        (application as MySitesApplication).siteStore.flush()
        super.onStop()
    }
}
