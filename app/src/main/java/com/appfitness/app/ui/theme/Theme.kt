package com.appfitness.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F2CE),
    onPrimaryContainer = Color(0xFF052914),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB6ECE7),
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD5),
    onTertiaryContainer = Color(0xFF410002),
    background = Color(0xFFF7FAF8),
    onBackground = Ink,
    surface = Color(0xFFF7FAF8),
    onSurface = Ink,
    surfaceVariant = Color(0xFFDCE5DE),
    onSurfaceVariant = Color(0xFF414941),
    outlineVariant = Color(0xFFC0C9C0),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6BDB97),
    onPrimary = Color(0xFF003919),
    primaryContainer = BrandGreenDark,
    onPrimaryContainer = Color(0xFFB8F2CE),
    secondary = Color(0xFF54D8CC),
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF00504A),
    onSecondaryContainer = Color(0xFFB6ECE7),
    tertiary = Color(0xFFFFB4AB),
    onTertiary = Color(0xFF690005),
    tertiaryContainer = Color(0xFF93000A),
    onTertiaryContainer = Color(0xFFFFDAD5),
    background = Color(0xFF0F1512),
    onBackground = Color(0xFFE1E3DE),
    surface = Color(0xFF0F1512),
    onSurface = Color(0xFFE1E3DE),
    surfaceVariant = Color(0xFF414941),
    onSurfaceVariant = Color(0xFFC0C9C0),
)

/** Rounded, contemporary shapes. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun AppFitnessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Use the brand identity by default; opt into Material You dynamic colour.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
