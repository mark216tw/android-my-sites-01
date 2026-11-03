package com.example.mysites.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mysites.data.SettingsStore
import com.example.mysites.data.SiteStore
import com.example.mysites.ui.home.HomeScreen
import com.example.mysites.ui.settings.SettingsScreen
import com.example.mysites.ui.web.SiteWebScreen

@Composable
fun MySitesApp(
    store: SiteStore,
    settingsStore: SettingsStore,
) {
    val loaded by store.loaded.collectAsStateWithLifecycle()

    if (!loaded) {
        AppLoadingScreen()
        return
    }

    val sites by store.sites.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val startSiteId = remember {
        settingsStore.lastSiteId()?.takeIf { id -> sites.any { it.id == id } }
    }

    LaunchedEffect(Unit) {
        val savedId = settingsStore.lastSiteId()
        if (savedId != null && sites.none { it.id == savedId }) {
            settingsStore.setLastSiteId(null)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startSiteId?.let { "site/$it" } ?: "home",
    ) {
        composable("home") {
            HomeScreen(
                store = store,
                settingsStore = settingsStore,
                onOpenSite = { site ->
                    navController.navigate("site/${site.id}")
                },
                onOpenSettings = {
                    navController.navigate("settings") {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable("settings") {
            SettingsScreen(
                settingsStore = settingsStore,
                store = store,
                onBack = { navController.popBackStack() },
            )
        }

        composable("site/{siteId}") { entry ->
            val siteId = entry.arguments?.getString("siteId")
            val site = sites.firstOrNull { it.id == siteId }

            if (site == null) {
                LaunchedEffect(siteId) {
                    settingsStore.setLastSiteId(null)
                    navController.goHome()
                }
            } else {
                LaunchedEffect(site.id) {
                    store.markUsed(site.id)
                    settingsStore.setLastSiteId(site.id)
                }

                SiteWebScreen(
                    site = site,
                    initialUrl = settingsStore.lastWebUrl(site.id) ?: site.url,
                    onTextZoomChange = { zoom ->
                        store.update(site.copy(textZoom = zoom))
                    },
                    onSaveLastUrl = { url ->
                        settingsStore.setLastWebUrl(site.id, url)
                    },
                    onBack = {
                        settingsStore.setLastSiteId(null)
                        if (!navController.popBackStack()) {
                            navController.goHome()
                        }
                    },
                )
            }
        }
    }
}

private fun NavHostController.goHome() {
    navigate("home") {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun AppLoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
