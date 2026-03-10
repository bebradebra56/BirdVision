package com.vision.birdvisionpr.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Amber80,
    onPrimary = Color(0xFF3E2000),
    primaryContainer = Color(0xFF5A3800),
    onPrimaryContainer = Amber80,
    secondary = LeafGreen80,
    onSecondary = Color(0xFF00390A),
    secondaryContainer = Color(0xFF1E5220),
    onSecondaryContainer = LeafGreen80,
    tertiary = EarthBrown80,
    onTertiary = Color(0xFF3E2000),
    background = DarkBackground,
    onBackground = Color(0xFFF5E6D0),
    surface = DarkSurface,
    onSurface = Color(0xFFF5E6D0),
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFFD9C4A8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = Amber40,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDDB0),
    onPrimaryContainer = Color(0xFF2E1500),
    secondary = LeafGreen40,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC8EFC0),
    onSecondaryContainer = Color(0xFF002205),
    tertiary = EarthBrown40,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCBD),
    onTertiaryContainer = Color(0xFF2C1500),
    background = WarmCream,
    onBackground = Color(0xFF1E1200),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E1200),
    surfaceVariant = WarmSurface,
    onSurfaceVariant = Color(0xFF4D3D2C),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun BirdVisionTheme(
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
        typography = Typography,
        content = content
    )
}
