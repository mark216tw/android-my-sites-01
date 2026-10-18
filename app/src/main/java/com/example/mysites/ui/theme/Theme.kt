package com.example.mysites.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.mysites.data.DEFAULT_THEME_HUE
import com.example.mysites.data.ThemeMode

@Composable
fun MySitesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themeHue: Int = DEFAULT_THEME_HUE,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = remember(themeHue, darkTheme) {
        colorSchemeFor(themeHue, darkTheme)
    }

    SystemBarsAppearance(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

@Composable
private fun SystemBarsAppearance(darkTheme: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current

    LaunchedEffect(darkTheme, view) {
        val activity = context.findActivity() ?: return@LaunchedEffect
        WindowCompat.getInsetsController(activity.window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
