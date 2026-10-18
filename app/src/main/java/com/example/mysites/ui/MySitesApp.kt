package com.example.mysites.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val navController = rememberNavController()
    val sites by store.sites.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            HomeScreen(
                store = store,
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
                onBack = { navController.popBackStack() },
            )
        }

        composable("site/{siteId}") { entry ->
            val siteId = entry.arguments?.getString("siteId")
            val site = sites.firstOrNull { it.id == siteId }

            if (site == null) {
                LaunchedEffect(siteId) {
                    navController.popBackStack()
                }
            } else {
                SiteWebScreen(
                    site = site,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
