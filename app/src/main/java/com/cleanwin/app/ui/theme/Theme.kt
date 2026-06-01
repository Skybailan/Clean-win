package com.cleanwin.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Brand8,
    onPrimary = Brand1,
    primaryContainer = Brand3,
    onPrimaryContainer = Brand10,
    secondary = Brand7,
    onSecondary = Brand2,
    secondaryContainer = Brand2,
    onSecondaryContainer = Brand9,
    tertiary = Brand6,
    onTertiary = Brand1,
    background = Brand1,
    surface = Brand1,
    onBackground = Brand10,
    onSurface = Brand10,
    surfaceVariant = Brand2,
    onSurfaceVariant = Brand8,
    outline = Brand6,
    error = Brand0,
    onError = Brand10
)

private val LightColorScheme = lightColorScheme(
    primary = Brand2,
    onPrimary = Brand11,
    primaryContainer = Brand9,
    onPrimaryContainer = Brand1,
    secondary = Brand4,
    onSecondary = Brand11,
    secondaryContainer = Brand10,
    onSecondaryContainer = Brand2,
    tertiary = Brand5,
    onTertiary = Brand11,
    background = Brand10,
    surface = Brand10,
    onBackground = Brand1,
    onSurface = Brand1,
    surfaceVariant = Brand8,
    onSurfaceVariant = Brand2,
    outline = Brand5,
    error = Brand2,
    onError = Brand11
)

@Composable
fun CleanWinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
