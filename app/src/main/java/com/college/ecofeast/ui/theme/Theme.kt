package com.college.ecofeast.ui.theme

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
    primary = SageGreen,
    secondary = PrimaryForestGreen,
    tertiary = SecondaryText,
    background = Color(0xFF19231B),
    surface = Color(0xFF19231B),
    onPrimary = Color.White,
    onBackground = WarmIvory,
    onSurface = WarmIvory
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryForestGreen,
    secondary = SageGreen,
    tertiary = SecondaryText,
    background = WarmIvory,
    surface = WarmIvory,
    onPrimary = Color.White,
    onSecondary = DeepText,
    onBackground = DeepText,
    onSurface = DeepText
)

@Composable
fun EcoFeastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+, disabled for consistent branding
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}