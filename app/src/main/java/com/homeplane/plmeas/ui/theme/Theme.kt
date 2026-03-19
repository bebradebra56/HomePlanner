package com.homeplane.plmeas.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = CardWhite,
    primaryContainer = TerracottaLight,
    onPrimaryContainer = TerracottaDark,
    secondary = Olive,
    onSecondary = CardWhite,
    secondaryContainer = OliveLight,
    onSecondaryContainer = TextDark,
    tertiary = DustyBlue,
    onTertiary = CardWhite,
    tertiaryContainer = DustyBlueLight,
    onTertiaryContainer = TextDark,
    background = WarmBackground,
    onBackground = TextDark,
    surface = CardWhite,
    onSurface = TextDark,
    surfaceVariant = SurfaceWarm,
    onSurfaceVariant = TextMedium,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inversePrimary = TerracottaLight
)

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaLight,
    onPrimary = TerracottaDark,
    primaryContainer = TerracottaDark,
    onPrimaryContainer = TerracottaLight,
    secondary = OliveLight,
    onSecondary = TextDark,
    secondaryContainer = Olive,
    onSecondaryContainer = OliveLight,
    tertiary = DustyBlueLight,
    onTertiary = TextDark,
    tertiaryContainer = DustyBlue,
    onTertiaryContainer = DustyBlueLight,
    background = DarkBackground,
    onBackground = CardWhite,
    surface = DarkSurface,
    onSurface = CardWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = OutlineVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline
)

@Composable
fun HomePlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
