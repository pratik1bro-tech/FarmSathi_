package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FarmGreenDarkPrimary,
    onPrimary = Color(0xFF003915),
    primaryContainer = FarmGreenDarkPrimaryContainer,
    secondary = FarmHarvestGold,
    tertiary = FarmEarthBrown,
    background = FarmBackgroundDark,
    surface = FarmSurfaceDark,
    surfaceVariant = Color(0xFF1E2A20)
)

private val LightColorScheme = lightColorScheme(
    primary = FarmGreenPrimary,
    onPrimary = FarmGreenOnPrimary,
    primaryContainer = FarmGreenPrimaryContainer,
    onPrimaryContainer = FarmGreenOnPrimaryContainer,
    secondary = FarmHarvestGold,
    secondaryContainer = FarmHarvestGoldContainer,
    onSecondaryContainer = Color(0xFFBF360C),
    tertiary = FarmEarthBrown,
    tertiaryContainer = FarmEarthContainer,
    onTertiaryContainer = Color(0xFF1B5E20),
    background = FarmBackgroundLight,
    surface = FarmSurfaceLight,
    surfaceVariant = FarmSurfaceVariantLight,
    onBackground = FarmOnSurfaceLight,
    onSurface = FarmOnSurfaceLight,
    onSurfaceVariant = FarmOnSurfaceVariantLight,
    outline = FarmOutlineLight,
    outlineVariant = Color(0xFFE1E5E0)
)

@Composable
fun FarmSathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our custom agricultural palette by default
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
